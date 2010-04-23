
import org.restlet.Application

def get_shared key, deflt=nil
	attributes = Application.current.context.attributes
	value = attributes[key]
	if value.nil? && !deflt.nil?
		value = deflt
		existing = attributes.put_if_absent key, deflt
		if !existing.nil?
			value = existing
		end
	end
	return value
end

def set_shared key, value
	attributes = Application.current.context.attributes
	attributes[key] = value
	return value
end
