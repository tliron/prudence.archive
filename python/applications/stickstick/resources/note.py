sys.path.append(str(document.container.source.basePath) + '/../libraries/')

from stickstick.data import *
from org.restlet.data import MediaType, Status
from org.restlet.representation import Variant

import minjson as json

def get_id():
    return int(document.container.resource.request.attributes.get('id'))
    #form = document.container.resource.request.resourceRef.queryAsForm
    #return int(form.getFirstValue('id'))

def handleInit():
    document.container.variants.add(Variant(MediaType.TEXT_PLAIN))
    document.container.variants.add(Variant(MediaType.APPLICATION_JSON))

def handleGet():
    id = get_id()
   
    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    finally:
        session.close()

    if note is None:
        document.container.resource.response.status = Status.CLIENT_ERROR_NOT_FOUND;
        return None

    document.container.modificationDateAsLong = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleGetInfo():
    id = get_id()
   
    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    finally:
        session.close()

    if note is None:
        document.container.resource.response.status = Status.CLIENT_ERROR_NOT_FOUND;
        return None

    return datetime_to_milliseconds(note.timestamp)

def handlePost():
    id = get_id()

    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = document.container.entity.text
    dict = json.read(text)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
        if note is None:
            document.container.resource.response.status = Status.CLIENT_ERROR_NOT_FOUND;
            return None

        note.update(dict)
        session.flush()
    finally:
        session.close()

    document.container.modificationDateAsLong = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleDelete():
    id = get_id()

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
        if note is None:
            document.container.resource.response.status = Status.CLIENT_ERROR_NOT_FOUND;
            return None

        session.delete(note)
        session.flush()
    finally:
        session.close()

    return None
