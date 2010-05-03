import sys
sys.path.append('%s/../libraries/' % prudence.source.basePath)

from sqlalchemy.orm.exc import NoResultFound
import minjson as json

prudence.execute('../libraries/stickstick/data/')

def get_id(conversation):
    try:
        return int(conversation.resource.request.attributes.get('id'))
    except ValueError:
        return None
    except TypeError:
        return None
    #form = conversation.resource.request.resourceRef.queryAsForm
    #return int(form.getFirstValue('id'))

def handle_init(conversation):
    conversation.addMediaTypeByName('text/plain')
    conversation.addMediaTypeByName('application/json')

def handle_get(conversation):
    id = get_id(conversation)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return 404
    finally:
        session.close()

    conversation.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handle_get_info(conversation):
    id = get_id(conversation)

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).one()
    except NoResultFound:
        return None
    finally:
        session.close()

    return datetime_to_milliseconds(note.timestamp)

def handle_post(conversation):
    id = get_id(conversation)

    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = conversation.entity.text
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

    conversation.modificationTimestamp = datetime_to_milliseconds(note.timestamp)
    return json.write(note.to_dict())

def handle_delete(conversation):
    id = get_id(conversation)

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
