<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Gestion des départements - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2">Gestion des départements</h1>
        <a class="btn btn-primary"
           href="${pageContext.request.contextPath}/admin/departments/new">
            Ajouter un département
        </a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${param.status eq 'created' or param.status eq 'updated'
                  or param.status eq 'deleted'}">
        <div class="alert alert-success" role="status">
            <c:choose>
                <c:when test="${param.status eq 'created'}">Département créé.</c:when>
                <c:when test="${param.status eq 'updated'}">Département modifié.</c:when>
                <c:when test="${param.status eq 'deleted'}">Département supprimé.</c:when>
            </c:choose>
        </div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Nom</th>
                <th scope="col">Description</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${departements}" var="departement">
                <tr>
                    <td><c:out value="${departement.nom}"/></td>
                    <td><c:out value="${departement.description}"/></td>
                    <td>
                        <a class="btn btn-sm btn-outline-primary mb-1"
                           href="${pageContext.request.contextPath}/admin/departments/edit?id=${departement.id}">
                            Modifier
                        </a>
                        <a class="btn btn-sm btn-outline-secondary mb-1"
                           href="${pageContext.request.contextPath}/admin/departments/team?id=${departement.id}">
                            Équipe et responsable
                        </a>
                        <form class="d-inline"
                              action="${pageContext.request.contextPath}/admin/departments/delete"
                              method="post"
                              onsubmit="return confirm('Supprimer ce département ?');">
                            <input type="hidden" name="id" value="${departement.id}">
                            <button class="btn btn-sm btn-outline-danger mb-1"
                                    type="submit">
                                Supprimer
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty departements}">
                <tr>
                    <td colspan="3" class="text-center">
                        Aucun département enregistré.
                    </td>
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
