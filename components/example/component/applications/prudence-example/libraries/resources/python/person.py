
class Person:
    def handle_init(self, conversation):
        conversation.addMediaTypeByName('text/html')
        conversation.addMediaTypeByName('text/plain')
        
    def handle_get(self, conversation):
        id = conversation.locals['id']
        return 'I am person %s, formatted as "%s", encased in Python'  % (id, conversation.mediaTypeName)
