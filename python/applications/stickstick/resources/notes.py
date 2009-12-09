sys.path.append(str(document.container.source.basePath) + '/../libraries/')

from stickstick.data import *
from org.restlet.data import MediaType
from org.restlet.representation import Variant

import minjson as json

def handleInit():
    document.container.variants.add(Variant(MediaType.TEXT_PLAIN))
    document.container.variants.add(Variant(MediaType.APPLICATION_JSON))

def handleGet():
    session = get_session()
    try:
        notes = session.query(Note).all()
        
        list = []
        for note in notes:
            list.append(note.to_dict())
    finally:
        session.close()

    return json.write(list)

def handlePut():
    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = document.container.entity.text
    dict = json.read(text)
    note = Note.from_dict(dict)
    
    session = get_session()
    try:
        session.add(note)
        session.flush()
    finally:
        session.close()
    
    return handleGet()
