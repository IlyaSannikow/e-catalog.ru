<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="utf-8">
  <title>Список групп</title>
  <link rel="stylesheet" type="text/css" href="${contextPath}/resources/css/style.css">
</head>

<body>
<div>
  <table>
    <thead>

    <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">
        <th>ID</th>
    </sec:authorize>

    <th>ФИО</th>
    <th>Сообщение</th>
    <th>Дата происшествия</th>
    <th>Внешний вид</th>
    <th>Номер телефона</th>

    <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">
        <th>Удалить</th>
        <th>Обновить</th>
    </sec:authorize>

    </thead>

    <c:forEach items="${allPost}" var="post">
      <tr>
        <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">
            <td>${post.id}</td>
        </sec:authorize>

        <td>${post.name}</td>
        <td>${post.message}</td>
        <td>${post.incidentDay}</td>
	    <td>${post.outfit}</td>
        <td>${post.phoneNumber}</td>

        <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">
            <td>
               <form action="${pageContext.request.contextPath}/postList" method="post">
                 <input type="hidden" name="postId" value="${post.id}"/>
                 <input type="hidden" name="action" value="delete"/>
                 <button type="submit">Удалить</button>
               </form>
             </td>

            <td>
               <form action="${pageContext.request.contextPath}/postList" method="post">
                 <input type="hidden" name="postId" value="${post.id}"/>
                 <input type="hidden" name="action" value="update"/>
                 <button type="submit">Обновить</button>
               </form>
             </td>
         </sec:authorize>

      </tr>
    </c:forEach>
  </table>

  <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">

  <h2>Добавить новый пост</h2>

      <div>

          <form action="${pageContext.request.contextPath}/postList" method="post">
           <input type="hidden" name="postId" value="${post.id}"/> <br>
           <input type="text" name="name" value="${post.name}" placeholder="Введите ФИО"/> <br>
           <input type="text" name="message" value="${post.message}" placeholder="Введите проишествие и данные"/> <br>
           <input type="date" name="incidentDay" value="${post.incidentDay}"/> Дата происшествия <br>
           <input type="text" name="outfit" value="${post.outfit}" placeholder="Введите одежду"/> <br>
           <input type="text" name="phoneNumber" value="${post.phoneNumber}" placeholder="Введите номер телефона"/> <br>
           <input type="hidden" name="action" value="create"/>
           <button type="submit">Создать</button>
         </form>

      </div>

  </sec:authorize>

  <a href="/">Главная</a>
</div>
</body>
</html>