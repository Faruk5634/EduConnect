const fs = require('fs');
const path = require('path');

const entityDir = 'd:\\EduConnect\\src\\main\\java\\com\\educonnect\\model';
const entities = ["User.java", "Student.java", "Teacher.java", "Parent.java", "Classroom.java", "Announcement.java", "Message.java"];

const importStr = "import org.hibernate.annotations.Filter;\nimport org.hibernate.annotations.FilterDef;\nimport org.hibernate.annotations.ParamDef;\n";
const annotationStr = `@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "schoolId", type = Long.class))\n@Filter(name = "tenantFilter", condition = "school_id = :schoolId")\n`;

for (const entity of entities) {
    const filePath = path.join(entityDir, entity);
    let content = fs.readFileSync(filePath, 'utf-8');

    if (content.includes('@FilterDef')) continue;

    content = content.replace(/(import jakarta\.persistence\.\*;)/, `$1\n${importStr}`);
    content = content.replace('@Entity', `${annotationStr}@Entity`);

    fs.writeFileSync(filePath, content, 'utf-8');
}
console.log('Entities updated.');
