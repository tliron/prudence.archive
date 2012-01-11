
class Person
  def handle_init conversation
    conversation.add_media_type_by_name 'text/html'
    conversation.add_media_type_by_name 'text/plain'
  end
      
  def handle_get conversation
    id = conversation.locals['id']
    return "I am person #{id}, formatted as \"#{conversation.mediaTypeName}\", encased in Ruby"
  end
end
