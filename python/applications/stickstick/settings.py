
document.container.include('defaults/application/settings')

application_name = 'Stickstick'
application_description = 'Share online sticky notes'
application_author = 'Tal Liron'
application_owner = 'Three Crickets'
application_home_url = 'http://threecrickets.com/prudence/stickstick/'
application_contact_email = 'prudence@threecrickets.com'

runtime_attributes.update({
    'stickstick.backend': 'mysql+zxjdbc',
    'stickstick.username': 'root',
    'stickstick.password': 'root',
    'stickstick.host': 'localhost',
    'stickstick.database': 'stickstick'
    })

show_debug_on_error = True

preheat_resources = ['notes/']
