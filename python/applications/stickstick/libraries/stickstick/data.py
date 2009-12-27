from sqlalchemy import Column, Integer, String, Text, DateTime
from threading import RLock
from org.restlet import Application
from datetime import datetime
from time import mktime

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

Base = declarative_base()
Session = sessionmaker()

engine = None
engine_lock = RLock()

def datetime_to_milliseconds(dt):
    return long(mktime(dt.utctimetuple()) * 1000) if dt else None

#
# Note
#

class Note(Base):
    
    id = Column(Integer, primary_key=True)
    board = Column(String(50, convert_unicode=True, assert_unicode='warn'), index=True)
    x = Column(Integer)
    y = Column(Integer)
    size = Column(Integer)
    content = Column(Text(convert_unicode=True, assert_unicode='warn'))
    timestamp = Column(DateTime)

    __tablename__ = 'note'
    
    @staticmethod
    def create_from_dict(dict):
        return Note(dict['board'], dict['x'], dict['y'], dict['size'], dict['content'])
    
    def __init__(self, board, x, y, size, content):
        self.board = board
        self.x = x
        self.y = y
        self.size = size
        self.content = content
        self.timestamp = datetime.now()
        
    def update(self, dict):
        if 'board' in dict:
            self.board = dict['board']
        if 'x' in dict:
            self.x = dict['x']
        if 'y' in dict:
            self.y = dict['y']
        if 'size' in dict:
            self.size = dict['size']
        if 'content' in dict:
            self.content = dict['content']
        self.timestamp = datetime.now()

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
# Helpers
#
        
def get_engine():
    engine_lock.acquire()
    try:
        global engine
        if engine is None:
            attributes = Application.getCurrent().context.attributes

            # Make sure database exists
            root_engine = create_engine('%s://%s:%s@%s/' % (attributes['stickstick.backend'], attributes['stickstick.username'], attributes['stickstick.password'], attributes['stickstick.host']))
            connection = root_engine.connect()
            #connection.execute('DROP DATABASE %s' % attributes['stickstick.database'])
            connection.execute('CREATE DATABASE IF NOT EXISTS %s' % attributes['stickstick.database'])
            connection.close()
    
            # Connect to database
            engine = create_engine('%s://%s:%s@%s/%s' % (attributes['stickstick.backend'], attributes['stickstick.username'], attributes['stickstick.password'], attributes['stickstick.host'], attributes['stickstick.database']))
            Session.configure(bind=engine)
            
            # Make sure tables exist
            Base.metadata.create_all(engine)
            
        return engine
    finally:
        engine_lock.release()

def get_connection():
    engine = get_engine()
    return engine.connect()

def get_session():
    get_engine()
    return Session()
        
#note = Note('fish', 200, 50)
#session = get_session()
#session.add(note)
#session.commit()
