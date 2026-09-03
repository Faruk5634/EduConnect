import os
import re

entity_dir = r"d:\EduConnect\src\main\java\com\educonnect\model"
entities = ["User.java", "Student.java", "Teacher.java", "Parent.java", "Classroom.java", "Announcement.java", "Message.java"]

import_str = "import org.hibernate.annotations.Filter;\nimport org.hibernate.annotations.FilterDef;\nimport org.hibernate.annotations.ParamDef;\n"

annotation_str = """@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "schoolId", type = Long.class))
@Filter(name = "tenantFilter", condition = "school_id = :schoolId")
"""

for entity in entities:
    path = os.path.join(entity_dir, entity)
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    if "@FilterDef" in content:
        continue

    # Add imports
    content = re.sub(r'(import jakarta\.persistence\.\*;)', r'\1\n' + import_str, content)
    
    # Add annotations before @Entity
    content = content.replace("@Entity", annotation_str + "@Entity")
    
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

print("Entities updated.")
