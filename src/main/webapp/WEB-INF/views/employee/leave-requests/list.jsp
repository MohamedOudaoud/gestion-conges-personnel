<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Mes demandes de congé - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2">Mes demandes de congé</h1>
        <a class="btn btn-primary"
           href="${pageContext.request.contextPath}/employee/leave-requests/new">
            Nouvelle demande
        </a>
    </div>

    <c:if test="${param.status eq 'created'}">
        <div class="alert alert-success" role="status">Demande créée.</div>
    </c:if>
    <c:if test="${param.status eq 'updated'}">
        <div class="alert alert-success" role="status">Demande modifiée.</div>
    </c:if>
    <c:if test="${param.status eq 'cancelled'}">
        <div class="alert alert-success" role="status">Demande annulée.</div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-bordered align-middle bg-white">
            <thead>
            <tr>
                <th scope="col">Type</th>
                <th scope="col">Début</th>
                <th scope="col">Fin</th>
                <th scope="col">Jours ouvrés</th>
                <th scope="col">Statut</th>
                <th scope="col">Motif</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${demandes}" var="demande">
                <tr>
                    <td><c:out value="${demande.typeConge.libelle}"/></td>
                    <td><c:out value="${demande.dateDebut}"/></td>
                    <td><c:out value="${demande.dateFin}"/></td>
                    <td><c:out value="${demande.nbJours ne null ? demande.nbJours : 0}"/></td>
                    <td><c:out value="${demande.statut}"/></td>
                    <td><c:out value="${demande.motif}"/></td>
                    <td>
                        <c:if test="${demande.statut eq 'EN_ATTENTE'}">
                            <a class="btn btn-sm btn-outline-primary mb-1"
                               href="${pageContext.request.contextPath}/employee/leave-requests/edit?id=${demande.id}">
                                Modifier
                            </a>
                            <form class="d-inline"
                                  action="${pageContext.request.contextPath}/employee/leave-requests/cancel"
                                  method="post">
                                <input type="hidden" name="id" value="${demande.id}">
                                <button class="btn btn-sm btn-outline-danger mb-1"
                                        type="submit"
                                        onclick="return confirm('Annuler cette demande ?');">
                                    Annuler
                                </button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty demandes}">
                <tr>
                    <td colspan="7" class="text-center">
                        Aucune demande de congé enregistrée.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <a class="btn btn-link px-0"
       href="${pageContext.request.contextPath}/dashboard">
        Retour au tableau de bord
    </a>
</main>
</body>
</html>
