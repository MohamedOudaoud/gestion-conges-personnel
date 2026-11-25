<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Gestion des employés - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2">Gestion des employés</h1>
        <a class="btn btn-primary"
           href="${pageContext.request.contextPath}/admin/employees/new">
            Ajouter un employé
        </a>
    </div>

    <c:if test="${param.status eq 'created' or param.status eq 'updated'
                  or param.status eq 'activated' or param.status eq 'deactivated'}">
        <div class="alert alert-success" role="status">
            <c:choose>
                <c:when test="${param.status eq 'created'}">Employé créé.</c:when>
                <c:when test="${param.status eq 'updated'}">Employé modifié.</c:when>
                <c:when test="${param.status eq 'activated'}">Compte activé.</c:when>
                <c:when test="${param.status eq 'deactivated'}">Compte désactivé.</c:when>
            </c:choose>
        </div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Matricule</th>
                <th scope="col">Nom</th>
                <th scope="col">Email</th>
                <th scope="col">Département</th>
                <th scope="col">Rôle</th>
                <th scope="col">État</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${employes}" var="employe">
                <tr>
                    <td><c:out value="${employe.matricule}"/></td>
                    <td><c:out value="${employe.prenom}"/> <c:out value="${employe.nom}"/></td>
                    <td><c:out value="${employe.email}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty employe.departement}">
                                <c:out value="${employe.departement.nom}"/>
                            </c:when>
                            <c:otherwise>Non défini</c:otherwise>
                        </c:choose>
                    </td>
                    <td><c:out value="${employe.role}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${employe.actif}">Actif</c:when>
                            <c:otherwise>Désactivé</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a class="btn btn-sm btn-outline-primary mb-1"
                           href="${pageContext.request.contextPath}/admin/employees/edit?id=${employe.id}">
                            Modifier
                        </a>
                        <form class="d-inline"
                              action="${pageContext.request.contextPath}/admin/employees/status"
                              method="post">
                            <input type="hidden" name="id" value="${employe.id}">
                            <input type="hidden" name="active" value="${not employe.actif}">
                            <button class="btn btn-sm btn-outline-secondary mb-1"
                                    type="submit">
                                <c:choose>
                                    <c:when test="${employe.actif}">Désactiver</c:when>
                                    <c:otherwise>Activer</c:otherwise>
                                </c:choose>
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty employes}">
                <tr>
                    <td colspan="7" class="text-center">Aucun employé enregistré.</td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <a class="btn btn-link px-0"
       href="${pageContext.request.contextPath}/dashboard/admin">
        Retour au tableau de bord
    </a>
</main>
</body>
</html>
