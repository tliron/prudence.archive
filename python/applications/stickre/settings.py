
document.container.include('defaults/application/settings')

application_name = 'Stickre'
application_description = 'Online sticky notes'
application_author = 'Tal Liron'
application_owner = 'Three Crickets'
application_home_url = 'http://www.threecrickets.com/prudence/stickre/'
application_contact_email = 'prudence@threecrickets.com'

runtime_attributes.update({
    'stickre.backend': 'mysql+zxjdbc',
    'stickre.username': 'root',
    'stickre.password': 'root',
    'stickre.host': 'localhost',
    'stickre.database': 'stickre'
    })