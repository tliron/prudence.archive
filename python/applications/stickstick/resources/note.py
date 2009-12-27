sys.path.append(str(document.container.source.basePath) + '/../libraries/')

from stickstick.data import *
from sqlalchemy.orm.exc import NoResultFound

import minjson as json

def get_id():
    return int(document.container.resource.request.attributes.get('id'))
    #form = document.container.resource.request.resourceRef.queryAsForm
    #return int(form.getFirstValue('id'))

def handleInit():
    document.container.addMediaTypeByName('text/plain')
    document.container.addMediaTypeByName('application/json')

def handleGet():
    id = get_id()
   
    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return 404
    finally:
        session.close()

    document.container.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleGetInfo():
    id = get_id()
   
    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return 404
    finally:
        session.close()

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
        note.update(dict)
        session.flush()
    except NoResultFound:
        return 404
    finally:
        session.close()

    document.container.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleDelete():
    id = get_id()

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
        session.delete(note)
        session.flush()
    except NoResultFound:
        return 404
    finally:
        session.close()

    return None
