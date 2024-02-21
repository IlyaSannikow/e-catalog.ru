<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru">
<meta charset="UTF-8">
<title>Обновить данные группы</title>
</head>
<body>
    <form action="${pageContext.request.contextPath}/groupList" method="post">
        <input type="text" name="name" value="${group.name}" placeHolder="Обновить имя группы"/> <br>
        <input type="hidden" name="groupId" value="${group.id}"/> <br>
        <input type="hidden" name="action" value="update2"/>
        <button type="submit">Обновить</button>
    </form>
</body>
</html>