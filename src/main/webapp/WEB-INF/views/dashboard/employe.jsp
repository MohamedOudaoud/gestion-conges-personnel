<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Espace employé - GestConges</title>
</head>
<body>
    <main>
        <h1>Espace employé</h1>
        <p>Bienvenue, <c:out value="${sessionScope.employePrenom}"/> <c:out value="${sessionScope.employeNom}"/>.</p>
        <c:if test="${not empty dashboardSummary}">
            <p>
                En attente : <strong>${dashboardSummary.pending}</strong>
                | Acceptées : <strong>${dashboardSummary.accepted}</strong>
                | Refusées : <strong>${dashboardSummary.rejected}</strong>
            </p>
        </c:if>
        <p>
            <a href="${pageContext.request.contextPath}/employee/leave-history">
                Consulter mon historique
            </a>
        </p>
        <p>
            <a href="${pageContext.request.contextPath}/employee/leave-requests">
                Mes demandes de congé
            </a>
        </p>
        <form action="${pageContext.request.contextPath}/logout" method="post">
            <button type="submit">Se déconnecter</button>
        </form>
    </main>
</body>
</html>
