
class Person:
    def handle_init(self, conversation):
        conversation.addMediaTypeByName('text/html')
        conversation.addMediaTypeByName('text/plain')
        
    def handle_get(self, conversation):
        id = conversation.locals['id']
        return 'I am person %s in %s'  % (id, conversation.mediaTypeName)

resources = {'person': Person()}
