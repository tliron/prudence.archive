sys.path.append(str(document.container.source.basePath) + '/../libraries/')

from stickre.data import *
from org.restlet.data import MediaType
from org.restlet.representation import Variant

import minjson as json

def get_id():
    #return document.container.resource.request.attributes.get('id')
    form = document.container.resource.request.resourceRef.queryAsForm
    return int(form.getFirstValue('id'))

def handleInit():
    document.container.variants.add(Variant(MediaType.TEXT_PLAIN))
    document.container.variants.add(Variant(MediaType.APPLICATION_JSON))

def handleGet():
    id = get_id()
   
    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).first()
    finally:
        session.close()

    return json.write(note.to_dict())

def handlePut():
    id = get_id()

def handleDelete():
    id = get_id()

    session = get_session()
    try:
        note = session.query(Note).filter_by(id=id).first()
        if note is not None:
            session.delete(note)
            session.commit()
    finally:
        session.close()

    return None
