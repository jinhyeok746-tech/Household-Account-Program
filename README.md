# Household-Account-Program
순수익 기반 맞춤형 저축 추천 시스템을 갖춘 개인 가계부입니다.

이 프로젝트의 핵심 목표는 단순한 수입/지출 기록을 넘어, 사용자가 자신의 재정 데이터를 기반으로 실질적인 재무 목표를 설정하고 달성할 수 있도록 돕는 것입니다.

재정 상태 시각화 , 저축 동기 부여, 쉬운 관리

----- 코드 설치 밒 실행방법 -----
필수 준비
1. IDE : Eclipse 또는 intelliJ IDEA 프로그램
2. SQLite JDBC(sqlite-jdbc-3.51.0.0.jar) 드라이버 다운

프로젝트 설정
IDE에서 SQLite DB를 사용하려면 다운로드한 JDBC파일을 프로젝트의 빌드 경로에 추가해야합니다.

Eclipse 기준:
1. 프로젝트 우클릭 -> Properties 선택
2. Java Build Path -> Libraries 선택
3. Add External JARs... 클릭 후 다운로드한 (sqlite-jdbc-3.51.0.0.jar)파일을 선택하여 추가

코드 실행 순서
1. 모든 자바 파일을 IDE프로젝트에 추가하고 빌드 경로 설정
2. MainApp.java 파일을 열고 main 메서드 실행
3. [LoginFrame]GUI가 실행되면 test/1234 (이미 회원가입이 되어있는 데이터)로 로그인하거나 새로운 계정을 회원가입하여 프로그램을 시작
4. 로그인 성공 후 수입/지출을 등록하거나 삭제
5. 수입/지출을 토대로 적금을 추천 
