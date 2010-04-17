sys.path.append('%s/../libraries/' % prudence.source.basePath)

from sqlalchemy.orm.exc import NoResultFound
from stickstick.data import *
import minjson as json

def get_id(resource):
    try:
        return int(resource.resource.request.attributes.get('id'))
    except ValueError:
        return None
    except TypeError:
        return None
    #form = resource.resource.request.resourceRef.queryAsForm
    #return int(form.getFirstValue('id'))

def handleInit(resource):
    resource.addMediaTypeByName('text/plain')
    resource.addMediaTypeByName('application/json')

def handleGet(resource):
    id = get_id(resource)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return 404
    finally:
        session.close()

    resource.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleGetInfo(resource):
    id = get_id(resource)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return None
    finally:
        session.close()

    return datetime_to_milliseconds(note.timestamp)

def handlePost(resource):
    id = get_id(resource)

    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = resource.entity.text
    note_dict = json.read(text)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
        note.update(note_dict)
        update_board_timestamp(session, note)
        session.flush()
    except NoResultFound:
        return 404
    finally:
        session.close()

    resource.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleDelete(resource):
    id = get_id(resource)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
        session.delete(note)
        update_board_timestamp(session, note, now())
        session.flush()
    except NoResultFound:
        return 404
    finally:
        session.close()

    return None
