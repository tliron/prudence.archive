
document.container.include('defaults/application/settings')

application_name = 'Stickstick'
application_description = 'Share online sticky notes'
application_author = 'Tal Liron'
application_owner = 'Three Crickets'
application_home_url = 'http://threecrickets.com/prudence/stickstick/'
application_contact_email = 'prudence@threecrickets.com'

runtime_attributes.update({
    'stickstick.backend': 'h2',
    'stickstick.username': 'root',
    'stickstick.password': 'root',
    'stickstick.host': '',
    'stickstick.database': 'h2/stickstick'
    })

show_debug_on_error = True

preheat_resources = ['notes/']
