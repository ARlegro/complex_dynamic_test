
## 목표 
**QueryDSL로 작성한 복잡한 동적 쿼리를 테스트하기** 
- QueryDSL 파일 : src/main/java/team7/hrbank/domain/department/CustomDepartmentRepositoryImpl
- QueryDSL 테스트 파일 : src/test/java/team7/hrbank/unit/department/RepositoryTest
___
### 참고 : 부서 페이지네이션 요구사항 
**부서 목록 조회**
- **이름 또는 설명**으로 부서 목록을 조회할 수 있습니다.
    - **{이름 또는 설명}**는 부분 일치 조건입니다.
    - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회합니다.
- **이름**, **설립일**로 정렬 및 페이지네이션을 구현합니다.
    - 여러 개의 정렬 조건 중 선택적으로 1개의 정렬 조건만 가질 수 있습니다.
    - 정확한 페이지네이션을 위해 **{이전 페이지의 마지막 요소 ID}**를 활용합니다.

이거에 맞춰서 QueryDSL코드를 짜봤는데 테스트를 하면서 일부 버그가 있는 것을 확인할 수 있었고 리팩토링함으로써 테스트를 성공할 수 있었다.

___
## 테스트 메인 흐름
##### 1. 더미 데이터 생성
##### 2. 조건값을 설정 
##### 3. 조건값을 pagination 코드에 넘겨주기 
##### 4. 가져온 페이지를 끝까지 do-while문으로 ehfflrl
##### 5. 검증하기 - 필터링 조건 성립, 개수 성립, 정렬 제대로 됐는지 


___
## 페이징 테스트 메인 로직 외 코드 설명 

### ✅더미 데이터 생성 코드 
```java
    private void setting_entity_save_and_containing_name(int entityCountOfNumber, String containingWord) {
        // 필요한 필드 : name, description, establishedment
        // entityCountOfNumber : 몇 개 생성하고 싶은지 
        // containingWord : query dsl에서 문자열을 기반으로 포함여부 필터링 있어서 
        Faker faker = new Faker();
        // 1. name 추출
        Set<String> departmentName = new HashSet<>();

        while (departmentName.size() < entityCountOfNumber) {
            String name = faker.company().name();  <<여기>>
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

        // 2. description 및 establishedDate 추출
        List<String> departmentDescription = new ArrayList<>();
        while (departmentDescription.size() < entityCountOfNumber) {
            String description = faker.lorem().paragraph(1);  <<여기>>
            departmentDescription.add(description);
        }

        List<LocalDate> establishedDate = new ArrayList<>();
        while (establishedDate.size() < entityCountOfNumber) {
            LocalDate randomDate = faker.date().birthdayLocalDate();  <<여기>>
            if (establishedDate.size() == entityCountOfNumber - 1) {
                establishedDate.add(randomDate); 
                // 일부러 두번 (establishedDate는 중복되면 그 다음 기준이 있기 때문에 그걸 테스트하고자)
            }
            establishedDate.add(randomDate);
        }


        List<String> nameList = departmentName.stream().toList();

        // 3. entity 저장
        List<Department> departmentList = new ArrayList<>();
        for (int i = 0; i < entityCountOfNumber; i++) {
            String name = nameList.get(i);
            String description = departmentDescription.get(i);
            LocalDate date = establishedDate.get(i);
            departmentList.add(new Department(name, description, date));
        }
        log.info("저장된 수 : {}", departmentList.size());
        departmentRepository.saveAllAndFlush(departmentList);
        em.clear();
    }


```
fake 라이브러릴 전에 더미 데이터를 만들기 위해 다양한 시도를 해봤다.
1. **[Mockaroo](https://mockaroo.com/)**  시도
   - 다양한 타입들의 dummy data를 랜덤으로 생성할 수 있다.
   - 이렇게 만든 JSON 데이터를 저장한 뒤 JsonPath로 추출하는 작업을 시도 
    ```java
    @Test
    void setUp() throws IOException {
        ClassPathResource jsonData = new ClassPathResource("department.json");
        // 이제 json 파싱이 필요
        // io 작업이니 inputstream으로 읽어야 함
        DocumentContext parsedData = JsonPath.parse(jsonData.getInputStream());
        List<DepartmentCreateRequest> jsonDtoList = parsedData.json();  // 알아서 매핑해줌
    ```
    - 한계 : JsonPath.parse()로 파싱할 경우 기본적으로 JSON 객체들을 LinkedHashMap으로 반환
    - 해결방법 : objcetMapper 사용 or jsonPath 문법 사용 or new TypeRef

    > 이런 방법들 다 사용해봤는데 괜히 복잡해져서 다른 방법을 알아보다가 Fake라이브러리 발견


2. ⭐**Fake 라이브러리 사용**
    - 다양한 타입의 랜덤 데이터를 만들 수 있다
    - 자바 기반이다보니 편리
    - 앞으로 이거를 쓸 것 같다.
    - setting_entity_save_and_containing_name 말고도 다른 필드에 공통 word를 포함하는 로직도 있다.



### ✅저장될 엔티티 수
```java

int entitySettingSize = 12;
int repeatCount = 5;
String otherNameOrDescriptionSize = "기서";
for (int i = 0; i < repeatCount; i++) {
    setting_entity_save_and_containing_name(entitySettingSize, searchCondition.getNameOrDescription());
    setting_entity_save_and_containing_name(entitySettingSize, otherNameOrDescriptionSize);
}
```
다른 단어들이 같이 존재하고 순서대로 저장되지 않도록 시뮬레이션하기 위해 



### ✅특수문자 처리 
이 부분에서도 꽤 시간을 많이 보냈던 것 같다.
테스트 중 넘어온 cotent들을 로그로 보면 얼추 정렬이 된 것 같은데, 이상하게 몇몇 부분만 테스트가 실패했다.
**문제가 되는 부분의 공통점은 특수문자와 공백이였다.**
> 이는, DB의 정렬 기준과 Java의 문자열 비교의 차이로 인해 발생한 문제였다.

DB마다 기준이 다르다고 하는데 이번엔 Postgre를 사용했기 때문에 postgre와 java의 차이를 알아보겠다.


DB는 로케일 기준으로 정렬하는데, Java는 유니코드/ASCII 기준이라 정렬 기준에서 충돌이 날 수 있다. 특히 특수문자 우선순위 면에서 큰 차이가 있는데 (값 - 왼쪽일수록 작은 값)
- **Postgre의 정렬**
  - 하이폰(-) < 쉼표 < 공백(OR 무시)
- **JAVA의 정렬**
  - 공백 < 쉼표 < 하이폰(-)

이를 해결하고자 하이폰이 가장 먼저오도록 정리된 데이터들을 아래와 같은 방법으로 수정하기로 했다.


```java
List<String> nameList = contentDTOList.stream()
        .map((dto) -> {
            String name = dto.name();
            return name.replaceAll("[-]+", "  "); // 하이픈(-)을 2번 공백으로 대체
        }).toList();
```
**✅ 하이폰(-)을 공백 2개로 하는 이유** 
만약, AvA, A-B 가 있다고 가정하자 (asc 기준)  **v = 공백**
DB에서 가져온 데이터는 A-B가 더 앞쪽에 있을 것이다. (하이폰이 공백보다 우선순위)
근데 **만약 공백하나**로 하이폰(-)을 대체한다면 A-B → AvB가 되고, 이는 AvA 보다 앞에 온 것으로 된다.(이는 자바 정렬기준과 다르다)
하지만, **공백을 2개**준다면 A-B → AvvB 가 되고 AvA보다 앞에 있는데, 이는 자바 정렬 기준에 충족한다
(특수기호는 문자보다 낮은 숫자)



### ✅ 대소문자 구분 없애기
**postgre는 기본적으로는 대소문자를 구분하지 않는 정렬을 사용**하기에 아래와 같이 사용
> String.CASE_INSENSITIVE_ORDER : Java에서 제공하는 정적 필드로, 문자열을 대소문자 구분 없이 사전 순으로 비교하는 Comparator<String> 객체

```java
        Comparator<String> caseInsensitiveOrder = String.CASE_INSENSITIVE_ORDER;

        if (sortDirection.trim().equalsIgnoreCase("desc")) {
            assertThat(nameList).as("내림차순 정렬")
                    .isSortedAccordingTo(caseInsensitiveOrder.reversed());
        } else {
            assertThat(nameList).as("오름차순 정렬")
                    .isSortedAccordingTo(caseInsensitiveOrder);
        }
```
