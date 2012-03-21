#
# This file is part of the Prudence Foundation Library
#
# Copyright 2011-2012 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.gnu.org/copyleft/lesser.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

$resources = {}

$document.execute_once $application.globals['prudence.dispatch.ruby.library']

def handle conversation, method
  id = conversation.locals['prudence.id']
  resource = $resources[id]
  if resource.nil?
    conversation.status_code = 404
    return nil
  end
  method = resource.method method
  if method.nil?
    conversation.status_code = 405
    return nil
  end
  return method.call conversation
end
    
def handle_init conversation
  handle conversation, :handle_init
end
    
def handle_get conversation
  return handle conversation, :handle_get
end

def handle_get_info conversation
  return handle conversation, :handle_get_info
end

def handle_post conversation
  return handle conversation, :handle_post
end

def handle_put conversation
  return handle conversation, :handle_put
end

def handle_delete conversation
  return handle conversation, :handle_delete
end
