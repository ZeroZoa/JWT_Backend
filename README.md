<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&height=300&section=header&text=Seungjun's%20Study%20Project&fontSize=50" alt="header" width="100%" />
</div>

<div align="center">
  <p>
    JWT를 이용해 Access Token, Refresh Token을 구현하고 테스트하기 위한 프로젝트입니다!
  </p>
</div>

<table align="center" width="100%">
  <thead>
    <tr>
      <th width="240" align="center"> 기능 </th>
      <th width="650"align="center"> 설명 </th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center"><b> 회원가입 </b></td>
      <td>
          일반 사용자 회원가입 <br/>
      </td>
    </tr>
    <tr>
      <td align="center"><b> 이메일 인증 </b></td>
      <td>
          일반 사용자 회원가입시 이메일 유효성 검사<br/>
      </td>
    </tr>
    <tr>
      <td align="center"><b> 로그인 </b></td>
      <td>
          일반 사용자의 Access Token, Refresh Token 발급 <br/>
      </td>
    </tr>
    <tr>
      <td align="center"><b> 비밀번호 찾기 및 재설정 </b></td>
      <td>
          패스워드 분실시 이메일 인증 후 비밀번호 재설정<br/>
      </td>
    </tr>
    <tr>
      <td align="center"><b> Access, Refresh Token </b></td>
      <td>
          Access Token이 만료되더라도 Refresh Token을 검사후 Access Token 재발급 <br/>
      </td>
    </tr>
    <tr>
      <td align="center"><b> 내 프로필 조회(권한 확인용) </b></td>
      <td>
          Access Token이 만료 후 Refresh Token을 통해 재발급 -> 로그인 유지 확인 <br/>
      </td>
    </tr>
  </tbody>
</table>

<br></br>

<div align="center">
  <h2 tabindex="-1" class="heading-element" dir="auto"> 사용기술입니다! </h2>
  <img src="https://img.shields.io/badge/Java-000000?style=for-the-badge&logo=&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/spring-000000?style=for-the-badge&logo=spring&logoColor=white" alt="Spring"/>
  <img src="https://img.shields.io/badge/spring_JPA-000000?style=for-the-badge&logo=&logoColor=white" alt="Spring JPA"/>
  <img src="https://img.shields.io/badge/spring_security-000000?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
</div>

<div align="center">
  <img src="https://img.shields.io/badge/PostgreSQL-000000?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Redis-000000?style=for-the-badge&logo=redis&logoColor=white" alt="Redis"/>
  <img src="https://img.shields.io/badge/Docker-000000?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/gradle-000000?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
</div>

<br></br>

<h1>Filter와 Interceptor의 차이점</h1>

<h2>Filter</h2>
<ul>
    <li>Servlet Container 내에서 실행된다.</li>
    <li>모든 요청에 대해 동작하며, Spring Context 외부의 요청과 응답도 처리할 수 있다.
        <br>→ Spring Security가 Spring MVC 밖에서도 작동할 수 있게 하는 핵심적인 이유.</li>
    <li>Spring Security는 자체 필터 체인을 통해 인증과 인가 과정을 관리한다.</li>
    <li>사용자 정의 필터를 자체 필터 체인에 쉽게 추가할 수 있다.</li>
    <li>필터는 요청이 DispatcherServlet에 도달하기 전에 실행된다.
        <br>→ 인증과 같이 모든 요청에 대해 처리해야 하는 로직에 적합하다.</li>
</ul>

<h2>Interceptor</h2>
<ul>
    <li>Spring MVC의 일부로, DispatcherServlet이 컨트롤러를 호출하기 전, 후, 완료 후에 동작한다.</li>
    <li>Spring Context 내부에서만 작동한다.</li>
    <li>주로 컨트롤러 실행을 가로채는 데 사용된다.</li>
    <li>요청 사전 처리, 로깅, 트랜잭션 관리 등에 적합하다.</li>
    <li>Spring Bean 접근이 가능하며 구성도 쉽다.</li>
</ul>

<h2> Filter vs Interceptor </h2>

<table border="1" cellspacing="0" cellpadding="4">
  <tr>
    <th width="500"align="center"> Filter</th>
    <th width="500"align="center">Interceptor</th>
  </tr>
  <tr>
    <td>DispatcherServlet Filter는 Dispatcher Servlet의 밖에 위치함<br>
      Web Context에 존재하며 Spring Context와 무관함</td>
    <td>
      DispatcherServlet Interceptor는 Dispatcher Servlet안에 위치함<br>
      Spring Context에 존재, 모든 Spring Bean에 접근 가능
    </td>
  </tr>
  <tr>
    <td>
      DispatcherServlet은<br>
      -SpringMVC의 프론트 컨트롤러이다.<br>
      -HTTP요청을 적절한 컨트롤러에 라우팅<br>
      -뷰 리졸버를 통해 렌더링<br>→ Filter 이후 DispatcherServletdl 요청을 받고,<br>
      그 다음 인터셉터가 컨트롤러 호출 직전 직후 동작</td>
  </tr>
  <tr>
    <td>Web context는<br>
      -서블릿 컨테이너의 레벨의 context가 요청을 관리하고있다는 것을 나타냄<br>
      -서블릿과 필터가 여기에 속함<br>
      -DispatcherServlet 바깥에서 요청과 응답을 가로채고 처리함</td>
    <td>Spring context는<br>
      -스프링 컨테이너의 애플리케이션 context가 요청을 관리한고있다는 것을 나타냄<br>
      -DispatcherServlet이 Spring Context를 사용하여 관리<br>
      → 필터는 Web context에 등록되어 Spring MVC 앞에서 동작하고, 인터셉터는 Spring MVC 안에서DispatcherServlet 이후 컨트롤러 앞뒤로 동작함
    </td>
  </tr>
</table>

<h2>DispatcherServlet</h2>
<ul>
    <li>Spring MVC의 프론트 컨트롤러</li>
    <li>HTTP 요청을 적절한 컨트롤러에 라우팅</li>
    <li>ViewResolver를 통해 렌더링 처리</li>
</ul>

<p><strong>요청 흐름:</strong> Filter → DispatcherServlet → Interceptor → Controller</p>

<h2>Web Context vs Spring Context</h2>

<h3>Web Context</h3>
<ul>
    <li>서블릿 컨테이너 레벨의 Context</li>
    <li>Filter, Servlet 등이 여기에 속함</li>
    <li>DispatcherServlet 바깥에서 요청/응답을 가로챔</li>
</ul>

<h3>Spring Context</h3>
<ul>
    <li>Spring 애플리케이션 컨텍스트</li>
    <li>DispatcherServlet이 Spring Context를 사용하여 컨트롤러/빈을 관리</li>
    <li>Interceptor는 Spring Context 내부에서 동작</li>
</ul>

<p><strong>결론:</strong><br>
Filter는 Web Context에서 Spring MVC 앞단에서 동작하고,<br>
Interceptor는 Spring MVC 내부에서 Controller 앞뒤로 동작한다.</p>

<h2>VirtualFilterChain</h2>
<ul>
    <li>Spring Security가 필터 체인을 실행하기 위해 내부적으로 사용하는 구현체</li>
    <li>여러 Security Filter를 순서대로 실행시키는 가상의 체인</li>
    <li>실제 클래스: <code>FilterChainProxy.VirtualFilterChain</code></li>
    <li>실무에서 JWT 인증은 대부분 필터 기반으로 구현</li>
    <li>Spring Security는 VirtualFilterChain을 통해 필터를 순서대로 실행</li>
</ul>

<h3>Interceptor는 언제 사용할까?</h3>
<ul>
    <li>단순 로깅</li>
    <li>특정 비즈니스 로직 전/후 처리</li>
    <li>컨트롤러 진입 전/후 공통 처리</li>
    <li>인증과 무관한 기능 처리</li>
</ul>
