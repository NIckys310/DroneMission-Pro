package com.dronemission.common.persistence;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Administrador de transacciones que hace funcionar @Transactional con MongoDB.
 *
 * Cada método @Transactional abre una {@link UnitOfWork}; al terminar sin error ("commit")
 * se guardan los documentos que cambiaron; si hubo un error ("rollback") los cambios pendientes
 * se descartan. Las transacciones anidadas (un servicio que llama a otro) comparten la misma
 * unidad de trabajo, igual que con JPA.
 *
 * Nota: lo que un servicio ya guardó con save() antes del error no se deshace, porque el
 * MongoDB local corre sin réplicas (requisito de las transacciones nativas de MongoDB).
 */
public class MongoUnitOfWorkTransactionManager extends AbstractPlatformTransactionManager {

    private final transient MongoTemplate mongoTemplate;

    public MongoUnitOfWorkTransactionManager(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /** Estado de una transacción: la unidad de trabajo a la que pertenece (si hay). */
    private static final class TxObject {
        UnitOfWork unitOfWork;
    }

    @Override
    protected Object doGetTransaction() {
        TxObject tx = new TxObject();
        tx.unitOfWork = (UnitOfWork) TransactionSynchronizationManager.getResource(UnitOfWork.KEY);
        return tx;
    }

    /** Si ya hay una unidad de trabajo abierta, el método se une a ella (propagación REQUIRED). */
    @Override
    protected boolean isExistingTransaction(Object transaction) {
        return ((TxObject) transaction).unitOfWork != null;
    }

    /** Empieza una transacción nueva: crea la unidad de trabajo y la asocia al hilo actual. */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        UnitOfWork unitOfWork = new UnitOfWork(definition.isReadOnly());
        ((TxObject) transaction).unitOfWork = unitOfWork;
        TransactionSynchronizationManager.bindResource(UnitOfWork.KEY, unitOfWork);
    }

    @Override
    protected Object doSuspend(Object transaction) {
        ((TxObject) transaction).unitOfWork = null;
        return TransactionSynchronizationManager.unbindResource(UnitOfWork.KEY);
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) {
        TransactionSynchronizationManager.bindResource(UnitOfWork.KEY, suspendedResources);
    }

    /** Commit: guarda en MongoDB los documentos que cambiaron. */
    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        ((TxObject) status.getTransaction()).unitOfWork.flush(mongoTemplate);
    }

    /** Rollback: no se guarda nada; los cambios pendientes en memoria se descartan. */
    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        // Intencionalmente vacío
    }

    /** Un método interno falló: la transacción exterior ya no podrá confirmar. */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        // AbstractPlatformTransactionManager ya marca el estado como "solo rollback"
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        TransactionSynchronizationManager.unbindResourceIfPossible(UnitOfWork.KEY);
        ((TxObject) transaction).unitOfWork = null;
    }
}
