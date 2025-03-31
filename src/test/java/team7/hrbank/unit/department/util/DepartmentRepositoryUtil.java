package team7.hrbank.unit.department.util;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DepartmentRepositoryUtil {

    private static final Faker faker = new Faker();
    private static final Logger log = LoggerFactory.getLogger(DepartmentRepositoryUtil.class);

    public static Set<String> get_department_name_common_Word(int entityCountOfNumber, String containingWord){
        Set<String> departmentName = new HashSet<>();
        while (departmentName.size() < entityCountOfNumber) {
            String name = faker.company().name();
            int randomNum = StringUtils.hasText(containingWord)
                    ? (int) (Math.random() * 3) + 1
                    : -1;

            switch (randomNum) {
                case 1 -> name = name + " " + containingWord;
                case 2 -> name = containingWord + " " + name;
                case 3 -> {
                    String zeroIndex = name.substring(0, 1);
                    String oneIndex = name.substring(1);
                    name = zeroIndex + containingWord + oneIndex;
                }
            }
            departmentName.add(name);
        }
        return departmentName;
    }

    public static Set<String> get_department_name(int entityCountOfNumber){
        Set<String> departmentName = new HashSet<>();
        while (departmentName.size() < entityCountOfNumber) {
            String name = faker.company().name();
            departmentName.add(name);
        }
        //departmentName.stream().toList()
        return departmentName;
    }

    public static List<String> get_department_description_common_word(int entityCountOfNumber, String containingWord){
        List<String> departmentDescription = new ArrayList<>();
        while (departmentDescription.size() < entityCountOfNumber) {
            String description = faker.lorem().paragraph(1);
            int randomNum = StringUtils.hasText(containingWord)
                    ? (int) (Math.random() * 3) + 1
                    : -1;
            switch (randomNum) {
                case 1 -> description = description + " " + containingWord;
                case 2 -> description = containingWord + " " + description;
                case 3 -> {
                    String zeroIndex = description.substring(0, 1);
                    String oneToEndIndex = description.substring(1);

                    description = zeroIndex + containingWord + oneToEndIndex;
                }
            }
            departmentDescription.add(description);
        }
        return departmentDescription;
    }

    public static List<String> get_department_description(int entityCountOfNumber){
        List<String> departmentDescription = new ArrayList<>();
        while (departmentDescription.size() < entityCountOfNumber) {
            String description = faker.lorem().paragraph(1);
            departmentDescription.add(description);
        }
        return departmentDescription;
    }


    public static List<LocalDate> get_LocalDates(int entityCount) {
        List<LocalDate> establishedDate = new ArrayList<>();
        while (establishedDate.size() < entityCount) {
            LocalDate randomDate = faker.date().birthdayLocalDate();
            if (establishedDate.isEmpty()) {
                establishedDate.add(randomDate); // 일부러 두번 (중복시키려고)
                log.info("중복된 establishedDate : {}", randomDate);
            }
            establishedDate.add(randomDate);
        }
        return establishedDate;
    }

}





