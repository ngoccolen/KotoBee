import re

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\home\HomeScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

code = code.replace('userProfile.skills_progress', 'userProfile.skillsProgress')
code = code.replace('userProfile.level_progress', '0.5f') # mock for now
code = code.replace('userProfile.next_level', 'if (userProfile.jlptLevel == "N5") "N4" else if (userProfile.jlptLevel == "N4") "N3" else if (userProfile.jlptLevel == "N3") "N2" else if (userProfile.jlptLevel == "N2") "N1" else "Max"')
code = code.replace('userProfile.current_level', 'userProfile.jlptLevel')

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\home\HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)
