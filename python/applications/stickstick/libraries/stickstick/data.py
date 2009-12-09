from sqlalchemy import Column, Integer, String, Text
from threading import RLock
from org.restlet import Application

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

Base = declarative_base()
Session = sessionmaker()

engine = None
engine_lock = RLock()

#
# Note
#

class Note(Base):
    
    id = Column(Integer, primary_key=True)
    board = Column(String(50), index=True)
    x = Column(Integer)
    y = Column(Integer)
    size = Column(Integer)
    content = Column(Text)

    __tablename__ = 'note'
    
    @staticmethod
    def from_dict(dict):
        return Note(dict['board'], dict['x'], dict['y'], dict['size'], dict['content'])
    
    def __init__(self, board, x, y, size, content):
        self.board = board
        self.x = x
        self.y = y
        self.size = size
        self.content = content
        
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
        
def connect():
    engine_lock.acquire()
    try:
        global engine
        if engine is None:
            attributes = Application.getCurrent().context.attributes

            # Make sure database exists
            root_engine = create_engine('%s://%s:%s@%s/' % (attributes['stickstick.backend'], attributes['stickstick.username'], attributes['stickstick.password'], attributes['stickstick.host']))
            connection = root_engine.connect()
            connection.execute('CREATE DATABASE IF NOT EXISTS %s' % attributes['stickstick.database'])
            connection.close()
    
            # Connect to database
            engine = create_engine('%s://%s:%s@%s/%s' % (attributes['stickstick.backend'], attributes['stickstick.username'], attributes['stickstick.password'], attributes['stickstick.host'], attributes['stickstick.database']))
            Session.configure(bind=engine)
            
            # Make sure tables exist
            Base.metadata.create_all(engine)
    finally:
        engine_lock.release()
    
def get_session():
    connect()
    return Session()
        
#note = Note('fish', 200, 50)
#session = get_session()
#session.add(note)
#session.commit()
