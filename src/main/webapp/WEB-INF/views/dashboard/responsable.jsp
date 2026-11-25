<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Espace responsable - GestConges</title>
</head>
<body>
    <main>
        <h1>Espace responsable</h1>
        <p>Bienvenue, <c:out value="${sessionScope.employePrenom}"/> <c:out value="${sessionScope.employeNom}"/>.</p>
        <c:if test="${not empty dashboardSummary}">
            <p>
                Équipe en attente : <strong>${dashboardSummary.pending}</strong>
                | Acceptées : <strong>${dashboardSummary.accepted}</strong>
                | Refusées : <strong>${dashboardSummary.rejected}</strong>
            </p>
        </c:if>
        <p>
            <a href="${pageContext.request.contextPath}/manager/leave-history">
                Consulter l'historique de mon équipe
            </a>
        </p>
        <p>
            <a href="${pageContext.request.contextPath}/employee/leave-requests">
                Mes demandes de congé
            </a>
        </p>
        <p>
            <a href="${pageContext.request.contextPath}/manager/leave-requests">
                Valider les demandes de mon équipe
            </a>
        </p>
        <form action="${pageContext.request.contextPath}/logout" method="post">
            <button type="submit">Se déconnecter</button>
        </form>
    </main>
</body>
</html>
