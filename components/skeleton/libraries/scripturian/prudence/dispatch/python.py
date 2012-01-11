
resources = {}

document.executeOnce(application.globals['prudence.dispatch.python.library'])

def handle(conversation, method):
    id = conversation.locals['prudence.id']
    resource = resources.get(id, None)
    if resource is None:
        conversation.statusCode = 404
        return None
    method = getattr(resource, method, None)
    if method is None:
        conversation.statusCode = 405
        return None
    return method(conversation)

def handle_init(conversation):
    handle(conversation, 'handle_init')

def handle_get(conversation):
    return handle(conversation, 'handle_get')

def handle_get_info(conversation):
    return handle(conversation, 'handle_get_info')

def handle_post(conversation):
    return handle(conversation, 'handle_post')

def handle_put(conversation):
    return handle(conversation, 'handle_put')

def handle_delete(conversation):
    return handle(conversation, 'handle_delete')
