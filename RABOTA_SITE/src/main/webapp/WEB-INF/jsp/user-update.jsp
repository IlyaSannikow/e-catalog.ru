<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru">
<meta charset="UTF-8">
<title>Обновить данные пользователя</title>
</head>
<body>
    <form action="${pageContext.request.contextPath}/admin" method="post">
        <input type="text" name="username" value="${user.username}" placeHolder="Обновить имя пользователя"/> <br>
        <input type="text" name="nickname" value="${user.nickname}" placeHolder="Обновить псевдоним пользователя"/> <br>
        <input type="hidden" name="userId" value="${user.id}"/> <br>
        <input type="hidden" name="action" value="update2"/>
        <button type="submit">Обновить</button>
    </form>
</body>
</html>