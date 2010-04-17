from sqlalchemy import Column, Integer, String, Text, DateTime
from sqlalchemy.orm.exc import NoResultFound
from threading import RLock
from org.restlet import Application
from datetime import datetime
from time import time, mktime

from sqlalchemy import create_engine
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import logging

# SQLAlchemy logging
logging.basicConfig(filename='logs/stickstick-python.log')
logging.getLogger('sqlalchemy.engine').setLevel(logging.DEBUG)
logging.getLogger('sqlalchemy.orm.attributes').setLevel(logging.DEBUG)
logging.getLogger('sqlalchemy.orm.mapper').setLevel(logging.DEBUG)
logging.getLogger('sqlalchemy.orm.unitofwork').setLevel(logging.DEBUG)
logging.getLogger('sqlalchemy.orm.strategies').setLevel(logging.DEBUG)
logging.getLogger('sqlalchemy.orm.sync').setLevel(logging.DEBUG)

Base = declarative_base()
Session = sessionmaker()

engine = None
engine_lock = RLock()

def datetime_to_milliseconds(dt):
    return long(mktime(dt.timetuple()) * 1000) if dt else 0

def now():
    return datetime.fromtimestamp(time())

#
# Note
#

class Note(Base):
    
    id = Column(Integer, primary_key=True)
    board = Column(String(50), index=True)
    x = Column(Integer)
    y = Column(Integer)
    size = Column(Integer)
    content = Column(Text())
    timestamp = Column(DateTime)

    __tablename__ = 'note'
    
    @staticmethod
    def create_from_dict(note_dict):
        return Note(note_dict['board'], note_dict['x'], note_dict['y'], note_dict['size'], note_dict['content'])
    
    def __init__(self, board, x, y, size, content):
        self.board = board
        self.x = x
        self.y = y
        self.size = size
        self.content = content
        self.timestamp = now()
        
    def update(self, note_dict):
        if 'board' in note_dict:
            self.board = note_dict['board']
        if 'x' in note_dict:
            self.x = note_dict['x']
        if 'y' in note_dict:
            self.y = note_dict['y']
        if 'size' in note_dict:
            self.size = note_dict['size']
        if 'content' in note_dict:
            self.content = note_dict['content']
        # TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
        # there is a chance that the note will not be set with the latest timestamp!
        self.timestamp = now()

    def to_dict(self):
        return {
                'id': self.id,
                'board': self.board,
                'x': self.x,
                'y': self.y,
                'size': self.size,
                'content': self.content
        }

#
# Board
#

class Board(Base):

    id = Column(String(50), primary_key=True)
    timestamp = Column(DateTime)

    __tablename__ = 'board'

    def __init__(self, id):
        self.id = id
        self.timestamp = now()
#
# Helpers
#
        
def get_engine(fresh=False):
    engine_lock.acquire()
    try:
        global engine
        if engine is None or fresh:
            attributes = Application.getCurrent().context.attributes

            # Make sure database exists
            if attributes['stickstick.host']:
                root_engine = create_engine('%s://%s:%s@%s/' % (
                    attributes['stickstick.backend'],
                    attributes['stickstick.username'],
                    attributes['stickstick.password'],
                    attributes['stickstick.host']),
                    convert_unicode=True)
                connection = root_engine.connect()
                if fresh:
                    connection.execute('DROP DATABASE %s' % attributes['stickstick.database'])
                connection.execute('CREATE DATABASE IF NOT EXISTS %s' % attributes['stickstick.database'])
                connection.close()

            # Connect to database
            engine = create_engine('%s://%s:%s@%s/%s' % (
                attributes['stickstick.backend'],
                attributes['stickstick.username'],
                attributes['stickstick.password'],
                attributes['stickstick.host'],
                attributes['stickstick.database']),
                convert_unicode=True,
                pool_recycle=3600)
            Session.configure(bind=engine)

            #connection = engine.connect()
            #connection.execute('SET AUTOCOMMIT ON')
            #connection.close()
            
            if not attributes['stickstick.host'] and fresh:
                if attributes['stickstick.backend'] == 'h2':
                    connection = engine.connect()
                    connection.execute('DROP ALL OBJECTS')
                    connection.close()
                else:
                    Base.metadata.drop_all(engine)
                    
            # Make sure tables exist
            Base.metadata.create_all(engine)

            # Make sure a few boards exist
            session = Session()
            try:
                session.add(Board('Todo List'))
                session.add(Board('Great Ideas'))
                session.add(Board('Sandbox'))
                session.flush()
            except IntegrityError:
                pass
            finally:
                session.close()
            
        return engine
    finally:
        engine_lock.release()

def get_connection(fresh=False):
    engine = get_engine(fresh)
    return engine.connect()

def get_session(fresh=False):
    get_engine(fresh)
    return Session()

def update_board_timestamp(session, note, timestamp=None):
    if timestamp is None:
        timestamp = note.timestamp

    try:
        board = session.query(Board).filter_by(id=note.board).one()
    except NoResultFound:
        board = Board(note.board)
        session.add(board)

    if timestamp > board.timestamp:
        # TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
        # there is a chance that the board will not be set with the latest timestamp!
        board.timestamp = timestamp

#note = Note('fish', 200, 50)
#session = get_session()
#session.add(note)
#session.commit()
