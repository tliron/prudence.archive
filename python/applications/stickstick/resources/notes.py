sys.path.append(str(prudence.source.basePath) + '/../libraries/')

from sqlalchemy.orm.exc import NoResultFound
from sqlalchemy.sql import func
import minjson as json
from stickstick.data import *

def handleInit():
    prudence.addMediaTypeByName('text/plain')
    prudence.addMediaTypeByName('application/json')

def handleGet():
    form = prudence.resource.request.resourceRef.queryAsForm
    fresh = form.getFirstValue('fresh') == 'true'

    session = get_session(fresh)
    try:
        notes = session.query(Note).all()

        max_timestamp = None
        note_list = []
        for note in notes:
            note_list.append(note.to_dict())
            timestamp = note.timestamp
            if max_timestamp is None or timestamp > max_timestamp:
                max_timestamp = timestamp
    except NoResultFound:
        return None
    finally:
        session.close()

    if max_timestamp is not None:
        prudence.modificationTimestamp = datetime_to_milliseconds(max_timestamp)
    return json.write(note_list)

def handleGetInfo():
    session = get_session()
    try:
        max_timestamp = session.query(func.max(Board.timestamp)).scalar()
    finally:
        session.close()
    return datetime_to_milliseconds(max_timestamp)

def handlePut():
    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = prudence.entity.text
    note_dict = json.read(text)
    note = Note.create_from_dict(note_dict)
    
    session = get_session()
    try:
        session.add(note)
        update_board_timestamp(session, note)
        session.flush()
    finally:
        session.close()
    
    return handleGet()
