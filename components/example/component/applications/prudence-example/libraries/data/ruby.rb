
# Helper to access the application globals

def get_global name
	value = $application.globals[name]
	if value.nil?
		value = yield

		# Note: another thread might have changed our value in the meantime.
		# We'll make sure there is no duplication.

		existing = $application.globals.put_if_absent name, value
		if not existing.nil?
			value = existing
		end

	end

	return value
end
