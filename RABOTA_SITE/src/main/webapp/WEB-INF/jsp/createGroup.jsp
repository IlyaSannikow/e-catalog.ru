<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Добавить группу</title>
</head>

<body>
<div>
  <form:form method="POST" modelAttribute="groupForm">
    <h2>Добавить группу</h2>
    <div>
      <form:input type="text" path="name" placeholder="Введите название" autofocus="true"></form:input>
    </div>
    <button type="submit">Добавить</button>
  </form:form>
  <a href="/">Главная</a>
</div>
</body>
</html>