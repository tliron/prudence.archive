from sqlalchemy import create_engine
from sqlalchemy import Column, Integer, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from threading import RLock

from org.restlet import Application

Base = declarative_base()
Session = sessionmaker()

engine = None
engine_lock = RLock()

#
# Note
#

class Note(Base):
    id = Column(Integer, primary_key=True)
    x = Column(Integer)
    y = Column(Integer)
    content = Column(Text)

    __tablename__ = 'note'
    
    @staticmethod
    def from_dict(dict):
        note = Note(dict['content'], dict['x'], dict['y'])
        return note
    
    @staticmethod
    def from_id(id):
        note = Note(None, None, None)
        note.id = id
        return note
    
    def __init__(self, content, x, y):
        self.content = content
        self.x = x
        self.y = y

    def to_dict(self):
        return {
                'id': self.id,
                'content': self.content,
                'x': self.x,
                'y': self.y
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
            root_engine = create_engine('%s://%s:%s@%s/' % (attributes['stickre.backend'], attributes['stickre.username'], attributes['stickre.password'], attributes['stickre.host']))
            connection = root_engine.connect()
            connection.execute('CREATE DATABASE IF NOT EXISTS %s' % attributes['stickre.database'])
            connection.close()
    
            # Connect to database
            engine = create_engine('%s://%s:%s@%s/%s' % (attributes['stickre.backend'], attributes['stickre.username'], attributes['stickre.password'], attributes['stickre.host'], attributes['stickre.database']))
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
