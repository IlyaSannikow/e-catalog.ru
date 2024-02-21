<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
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

    <th>ID</th>
    <th>Название</th>
    <th>Удалить</th>
    <th>Обновить</th>

    </thead>

    <c:forEach items="${allGroup}" var="group">
      <tr>

        <td>${group.id}</td>
        <td>${group.name}</td>

        <td>
           <form action="${pageContext.request.contextPath}/groupList" method="post">
             <input type="hidden" name="groupId" value="${group.id}"/>
             <input type="hidden" name="action" value="delete"/>
             <button type="submit">Удалить</button>
           </form>
         </td>

        <td>
           <form action="${pageContext.request.contextPath}/groupList" method="post">
             <input type="hidden" name="groupId" value="${group.id}"/>
             <input type="hidden" name="action" value="update"/>
             <button type="submit">Обновить</button>
           </form>
         </td>

      </tr>
    </c:forEach>
  </table>
  <a href="/">Главная</a>
</div>
</body>
</html>