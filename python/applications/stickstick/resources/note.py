sys.path.append(str(prudence.source.basePath) + '/../libraries/')

from sqlalchemy.orm.exc import NoResultFound

from stickstick.data import *

import minjson as json

def get_id():
    try:
        return int(prudence.resource.request.attributes.get('id'))
    except ValueError:
        return None
    except TypeError:
        return None
    #form = prudence.resource.request.resourceRef.queryAsForm
    #return int(form.getFirstValue('id'))

def handleInit():
    prudence.addMediaTypeByName('text/plain')
    prudence.addMediaTypeByName('application/json')

def handleGet():
    id = get_id()

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return 404
    finally:
        session.close()

    prudence.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleGetInfo():
    id = get_id()

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return None
    finally:
        session.close()

    return datetime_to_milliseconds(note.timestamp)

def handlePost():
    id = get_id()

    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = prudence.entity.text
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

    prudence.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handleDelete():
    id = get_id()

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
