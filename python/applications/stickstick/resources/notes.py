sys.path.append(str(document.container.source.basePath) + '/../libraries/')

from stickstick.data import *
from org.restlet.data import MediaType
from org.restlet.representation import Variant
from sqlalchemy.sql import func

import minjson as json

def handleInit():
    document.container.variants.add(Variant(MediaType.TEXT_PLAIN))
    document.container.variants.add(Variant(MediaType.APPLICATION_JSON))

def handleGet():
    session = get_session()
    try:
        notes = session.query(Note).all()

        max_timestamp = None
        list = []
        for note in notes:
            list.append(note.to_dict())
            timestamp = note.timestamp
            if max_timestamp is None or timestamp > max_timestamp:
                max_timestamp = timestamp
    finally:
        session.close()

    document.container.modificationDateAsLong = datetime_to_milliseconds(max_timestamp)
    return json.write(list)

def handleGetInfo():
    # Note that this is more efficient than handleGet()! If our notes have not
    # been changed since the client last fetched them, then handleGet() will not be
    # called
    session = get_session()
    try:
        max_timestamp = session.query(func.max(Note.timestamp)).scalar()
    finally:
        session.close()
    return datetime_to_milliseconds(max_timestamp)

def handlePut():
    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = document.container.entity.text
    dict = json.read(text)
    note = Note.create_from_dict(dict)
    
    session = get_session()
    try:
        session.add(note)
        session.flush()
    finally:
        session.close()
    
    return handleGet()
