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
  <table display:block>
    <thead>

    <th>ID</th>
    <th>Название</th>
    <th>Участники</th>
    <th>Вступить</th>
    <th>Выйти из группы</th>

    <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">
        <th>Удалить</th>
        <th>Обновить</th>
    </sec:authorize>

    </thead>

    <c:forEach items="${allGroup}" var="group">
      <tr>

        <td>${group.id}</td>
        <td>${group.name}</td>

        <td>
          <c:forEach items="${group.users}" var="user">
            Имя: ${user.username} <br>
            Номер: ${user.phoneNumber} <br>
            -------------------------- <br>
          </c:forEach>
        </td>

        <td>
            <form action="${pageContext.request.contextPath}/groupList" method="post" modelAttribute="groupForm">
               <input type="hidden" name="groupId" value="${group.id}"/>
               <input type="hidden" name="action" value="add"/>
               <button type="submit">Вступить в группу</button>
             </form>
         </td>

         <td>
            <form action="${pageContext.request.contextPath}/groupList" method="post" modelAttribute="groupForm">
                <input type="hidden" name="groupId" value="${group.id}"/>
                <input type="hidden" name="action" value="remove"/>
                <button type="submit">Выйти из группы</button>
            </form>
         </td>

        <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">
            <td>
               <form action="${pageContext.request.contextPath}/groupList" method="post">
                 <input type="hidden" name="groupId" value="${group.id}"/>
                 <input type="hidden" name="action" value="delete"/>
                 <button type="submit">Удалить группу</button>
               </form>
             </td>

            <td>
               <form action="${pageContext.request.contextPath}/groupList" method="post">
                 <input type="hidden" name="groupId" value="${group.id}"/>
                 <input type="hidden" name="action" value="update"/>
                 <button type="submit">Обновить группу</button>
               </form>
            </td>
        </sec:authorize>

      </tr>
    </c:forEach>
  </table>

  <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_POLICE')">

  <h2>Добавить новую группу</h2>

    <div>

        <form action="${pageContext.request.contextPath}/groupList" method="post">
         <input type="hidden" name="groupId" value="${group.id}"/> <br>
         <input type="text" name="name" value="${group.name}" placeholder="Введите название группы"/> <br>
         <input type="hidden" name="action" value="create"/>
         <button type="submit">Создать группу</button>
       </form>

    </div>

  </sec:authorize>
  <a href="/">Главная</a>
</div>
</body>
</html>