const fs = require('fs');

const file = 'd:\\EduConnect\\src\\main\\java\\com\\educonnect\\service\\StudentService.java';
let code = fs.readFileSync(file, 'utf-8');

// 1. Remove private methods
code = code.replace(/private School getTenantSchool\(User user\) \{[\s\S]*?return user\.getSchool\(\);\s*\}/, '');
code = code.replace(/private void assertStudentBelongsToTenant\(Student student, School tenantSchool\) \{[\s\S]*?throw new ResourceNotFoundException\("Öğrenci bulunamadı"\);\s*\}\s*\}/, '');
code = code.replace(/private void assertParentBelongsToTenant\(Parent parent, School tenantSchool\) \{[\s\S]*?throw new ResourceNotFoundException\("Veli bulunamadı"\);\s*\}\s*\}/, '');
code = code.replace(/private void assertClassroomBelongsToTenant\(Classroom classroom, School tenantSchool\) \{[\s\S]*?throw new ResourceNotFoundException\("Sınıf bulunamadı"\);\s*\}\s*\}/, '');

// 2. Add schoolRepository import if needed
if (!code.includes('SchoolRepository')) {
    code = code.replace('import com.educonnect.repository.StudentRepository;', 'import com.educonnect.repository.StudentRepository;\nimport com.educonnect.repository.SchoolRepository;');
    code = code.replace('private final StudentRepository studentRepository;', 'private final StudentRepository studentRepository;\n    private final SchoolRepository schoolRepository;');
}
if (!code.includes('TenantContext')) {
    code = code.replace('import com.educonnect.model.User;', 'import com.educonnect.model.User;\nimport com.educonnect.security.TenantContext;');
}

// 3. Helper for current school
const helper = `
    private School getCurrentSchool() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return null;
        return schoolRepository.getReferenceById(tenantId);
    }
`;
code = code.replace('public class StudentService {', 'public class StudentService {' + helper);

// 4. Refactor getAllStudents
code = code.replace(/public List<StudentDTO> getAllStudents\(\) \{[\s\S]*?return students[\s\S]*?collect\(Collectors\.toList\(\)\);\s*\}/, `public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }`);

// 5. Refactor getStudentBySchoolNumber
code = code.replace(/public Student getStudentBySchoolNumber\(String schoolNumber\) \{[\s\S]*?\}\s*public List<StudentDTO>/, `public Student getStudentBySchoolNumber(String schoolNumber) {
        return studentRepository.findBySchoolNumber(schoolNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bu okul numarasına ait bir öğrenci bulunamadı!"));
    }\n\n    public List<StudentDTO>`);

// 6. Refactor searchStudentsByFirstName
code = code.replace(/public List<StudentDTO> searchStudentsByFirstName\(String firstName\) \{[\s\S]*?collect\(Collectors\.toList\(\)\);\s*\}/, `public List<StudentDTO> searchStudentsByFirstName(String firstName) {
        return studentRepository.findByFirstNameContainingIgnoreCase(firstName).stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }`);

// 7. Refactor getStudentsPaginated
code = code.replace(/public Page<StudentDTO> getStudentsPaginated\(int page, int size\) \{[\s\S]*?findAll\(pageable\)\.map\(StudentMapper::toDto\);\s*\}/, `public Page<StudentDTO> getStudentsPaginated(int page, int size) {
        return studentRepository.findAll(PageRequest.of(page, size)).map(StudentMapper::toDto);
    }`);

fs.writeFileSync(file, code);
console.log('Done rewriting simple methods');
