# h2.py
#
# This module is part of SQLAlchemy and is released under
# the MIT License: http://www.opensource.org/licenses/mit-license.php
"""Support for the H2 database.

For information on connecting using a specific driver, see the documentation
section regarding that driver.
    
"""

import datetime, re, time

from sqlalchemy import schema as sa_schema
from sqlalchemy import sql, exc, pool, DefaultClause
from sqlalchemy.engine import default
from sqlalchemy.engine import reflection
from sqlalchemy import types as sqltypes
from sqlalchemy import util
from sqlalchemy.sql import compiler, functions as sql_functions
from sqlalchemy.util import NoneType

from sqlalchemy.types import BIGINT, BINARY, BLOB, BOOLEAN, CHAR, CLOB, DATE, DATETIME, DECIMAL,\
                            FLOAT, INTEGER, NUMERIC, SMALLINT, TEXT, TIME,\
                            TIMESTAMP, VARCHAR


#class H2TimeStamp(TIMESTAMP):
#    def get_col_spec(self):
#        return 'TIMESTAMP'

colspecs = {
#    sqltypes.TIMESTAMP: H2TimeStamp,
}

ischema_names = {
    'BIGINT': BIGINT,
    'BINARY': BINARY,
    'BLOB': BLOB,
    'BOOLEAN': BOOLEAN,
    'CHAR': CHAR,
    'CLOB': CLOB,
    'DATE': DATE,
    'DECIMAL': DECIMAL,
    #'DOUBLE': DOUBLE,
    'INT': INTEGER,
    #'REAL': REAL,
    'SMALLINT': SMALLINT,
    'TIME': TIME,
    'TIMESTAMP': TIMESTAMP,
    #'TINYINT': TINYINT,
    'VARCHAR': VARCHAR,
    #'VARCHAR_IGNORECASE': VARCHAR,
}


class H2Compiler(compiler.SQLCompiler):
    extract_map = compiler.SQLCompiler.extract_map.copy()

    def visit_now_func(self, fn, **kw):
        return "CURRENT_TIMESTAMP"

    def for_update_clause(self, select):
        return ''


class H2DDLCompiler(compiler.DDLCompiler):

    def get_column_specification(self, column, **kwargs):
        colspec = self.preparer.format_column(column) + " " + self.dialect.type_compiler.process(column.type)
        default = self.get_column_default_string(column)
        if default is not None:
            colspec += " DEFAULT " + default

        if not column.nullable:
            colspec += " NOT NULL"

        if column.primary_key and \
             len(column.table.primary_key.columns) == 1 and \
             isinstance(column.type, sqltypes.Integer) and \
             not column.foreign_keys:
             colspec += " PRIMARY KEY AUTO_INCREMENT"
            
        return colspec

class H2TypeCompiler(compiler.GenericTypeCompiler):
    pass

class H2IdentifierPreparer(compiler.IdentifierPreparer):
    reserved_words = set([
        'add', 'after', 'all', 'alter', 'analyze', 'and', 'as', 'asc',
        'attach', 'autoincrement', 'before', 'begin', 'between', 'by',
        'cascade', 'case', 'cast', 'check', 'collate', 'column', 'commit',
        'conflict', 'constraint', 'create', 'cross', 'current_date',
        'current_time', 'current_timestamp', 'database', 'default',
        'deferrable', 'deferred', 'delete', 'desc', 'detach', 'distinct',
        'drop', 'each', 'else', 'end', 'escape', 'except', 'exclusive',
        'explain', 'false', 'fail', 'for', 'foreign', 'from', 'full', 'glob',
        'group', 'having', 'if', 'ignore', 'immediate', 'in', 'index',
        'indexed', 'initially', 'inner', 'insert', 'instead', 'intersect', 'into', 'is',
        'isnull', 'join', 'key', 'left', 'like', 'limit', 'match', 'natural',
        'not', 'notnull', 'null', 'of', 'offset', 'on', 'or', 'order', 'outer',
        'plan', 'pragma', 'primary', 'query', 'raise', 'references',
        'reindex', 'rename', 'replace', 'restrict', 'right', 'rollback',
        'row', 'select', 'set', 'table', 'temp', 'temporary', 'then', 'to',
        'transaction', 'trigger', 'true', 'union', 'unique', 'update', 'using',
        'vacuum', 'values', 'view', 'virtual', 'when', 'where',
        ])

    def __init__(self, dialect, initial_quote="'", 
                    final_quote=None, escape_quote="'", omit_schema=False):
        super(H2IdentifierPreparer, self).__init__(dialect=dialect, initial_quote=initial_quote, final_quote=final_quote, escape_quote=escape_quote, omit_schema=omit_schema)

class H2Dialect(default.DefaultDialect):
    name = 'h2'
    supports_alter = True
    supports_unicode_statements = True
    supports_unicode_binds = True
    returns_unicode_strings = True
    supports_default_values = True
    supports_empty_insert = False
    supports_cast = True
    supports_native_boolean = True

    default_paramstyle = 'qmark'
    statement_compiler = H2Compiler
    ddl_compiler = H2DDLCompiler
    type_compiler = H2TypeCompiler
    preparer = H2IdentifierPreparer
    ischema_names = ischema_names
    colspecs = colspecs

    def do_begin(self, connect):
        cu = connect.cursor()
        cu.execute('SET AUTOCOMMIT ON')

    #def do_commit(self, connect):
    #    pass

    #def do_rollback(self, connect):
    #    pass
    
    def table_names(self, connection, schema):
        quote = self.identifier_preparer.quote_identifier
        if schema is not None:
            s = ("SELECT table_name FROM information_schema.tables "
                 "WHERE table_type='TABLE' AND table_schema=%s ORDER BY table_name") % (quote(schema.upper()),)
        else:
            s = ("SELECT table_name FROM information_schema.tables "
                "WHERE table_type='TABLE' AND table_schema='PUBLIC' ORDER BY table_name")
        
        rs = connection.execute(s)
        return [row[0] for row in rs]

    def has_table(self, connection, table_name, schema=None):
        quote = self.identifier_preparer.quote_identifier
        if schema is not None:
            s = ("SELECT table_name FROM information_schema.tables "
                      "WHERE table_type='TABLE' AND table_schema=%s AND table_name=%s") % (quote(schema.upper()), quote(table_name.upper()))
        else:
            s = ("SELECT table_name FROM information_schema.tables "
                      "WHERE table_type='TABLE' AND table_schema='PUBLIC' AND table_name=%s") % (quote(table_name.upper()),)

        rs = connection.execute(s)
        row = rs.fetchone()

        return (row is not None)

    @reflection.cache
    def get_table_names(self, connection, schema=None, **kw):
        return self.table_names(connection, schema)
