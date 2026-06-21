개인 일정/할 일 관리 프로그램

실행 방법
1. VS Code 또는 터미널에서 src 폴더로 이동
2. 컴파일: javac *.java
3. 실행: java TaskApp

주요 기능
- 할 일 추가
- 할 일 삭제
- 완료/취소 처리
- 파일 저장(tasks.dat)
- 파일 불러오기
- 현재 날짜 표시 스레드

사용된 Java 개념
- 클래스와 객체: Task, TaskManager, FileManager, TaskApp
- 상속: ImportantTask extends Task
- 컬렉션: ArrayList<Task>
- 예외처리: try-catch
- 파일 입출력: ObjectInputStream, ObjectOutputStream
- GUI: Java Swing(JFrame, JTable, JButton, JTextField, JTextArea)
- 스레드: 현재 날짜 표시
