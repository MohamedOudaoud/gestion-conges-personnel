<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Gestion des types de congé - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2">Gestion des types de congé</h1>
        <a class="btn btn-primary"
           href="${pageContext.request.contextPath}/admin/leave-types/new">
            Ajouter un type de congé
        </a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${param.status eq 'created' or param.status eq 'updated'
                  or param.status eq 'activated'
                  or param.status eq 'deactivated'}">
        <div class="alert alert-success" role="status">
            <c:choose>
                <c:when test="${param.status eq 'created'}">Type de congé créé.</c:when>
                <c:when test="${param.status eq 'updated'}">Type de congé modifié.</c:when>
                <c:when test="${param.status eq 'activated'}">Type de congé activé.</c:when>
                <c:when test="${param.status eq 'deactivated'}">Type de congé désactivé.</c:when>
            </c:choose>
        </div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Libellé</th>
                <th scope="col">Jours par an</th>
                <th scope="col">Décompte du solde</th>
                <th scope="col">Justificatif requis</th>
                <th scope="col">Statut</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${typesConge}" var="typeConge">
                <tr>
                    <td><c:out value="${typeConge.libelle}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${typeConge.joursParAn ne null}">
                                <c:out value="${typeConge.joursParAn}"/>
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </td>
                    <td><c:out value="${typeConge.decompteSolde ? 'Oui' : 'Non'}"/></td>
                    <td><c:out value="${typeConge.justificatifRequis ? 'Oui' : 'Non'}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${typeConge.actif}">Actif</c:when>
                            <c:otherwise>Inactif</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a class="btn btn-sm btn-outline-primary mb-1"
                           href="${pageContext.request.contextPath}/admin/leave-types/edit?id=${typeConge.id}">
                            Modifier
                        </a>
                        <form class="d-inline"
                              action="${pageContext.request.contextPath}/admin/leave-types/status"
                              method="post">
                            <input type="hidden" name="id" value="${typeConge.id}">
                            <input type="hidden" name="active"
                                   value="${typeConge.actif ? 'false' : 'true'}">
                            <button class="btn btn-sm btn-outline-secondary mb-1"
                                    type="submit">
                                <c:out value="${typeConge.actif ? 'Désactiver' : 'Activer'}"/>
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty typesConge}">
                <tr>
                    <td colspan="6" class="text-center">
                        Aucun type de congé enregistré.
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
