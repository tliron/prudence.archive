from sqlalchemy.dialects.h2 import zxjdbc
from sqlalchemy.dialects.h2 import base

# default dialect
base.dialect = zxjdbc.dialect
