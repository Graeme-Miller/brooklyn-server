package brooklyn.management;

import java.util.Collection;

import brooklyn.catalog.BrooklynCatalog;
import brooklyn.config.StringConfigMap;
import brooklyn.entity.Application;
import brooklyn.entity.Entity;
import brooklyn.entity.drivers.DriverDependentEntity;
import brooklyn.entity.drivers.EntityDriverManager;
import brooklyn.entity.drivers.downloads.DownloadResolverManager;
import brooklyn.entity.rebind.RebindManager;
import brooklyn.location.LocationRegistry;

/**
 * This is the entry point for accessing and interacting with a realm of applications and their entities in Brooklyn.
 *
 * For example, policies and the management console(s) (web-app, etc) can use this to interact with entities; 
 * policies, web-app, and entities share the realm for subscribing to events, executing tasks, and generally co-existing.      
 * <p>
 * It may refer to several applications, and it refers to all the entities descended from those applications.
 */
public interface ManagementContext {

    /** 
     * UID for the Brooklyn management plane which this {@link ManagementContext} node is a part of.
     * <p>
     * Each Brooklyn entity is actively managed by a unique management plane 
     * whose ID which should not normally change for the duration of that entity, 
     * even though the nodes in that plane might, and the plane may go down and come back up. 
     * In other words the value of {@link Application#getManagementContext()#getManagementPlaneId()} 
     * will generally be constant (in contrast to {@link #getManagementNodeId()}).
     * <p>
     * This value should not be null unless the management context is a non-functional
     * (non-deployment) instance. */
    String getManagementPlaneId();
    
    /** 
     * UID for this {@link ManagementContext} node (as part of a single management plane).
     * <p>
     * No two instances of {@link ManagementContext} should ever have the same node UID. 
     * The value of {@link Application#getManagementContext()#getManagementNodeId()} may
     * change many times (in contrast to {@link #getManagementPlaneId()}). 
     * <p>
     * This value should not be null unless the management context is a non-functional
     * (non-deployment) instance. */
    String getManagementNodeId();
    
    /**
     * All applications under control of this management plane
     */
    Collection<Application> getApplications();

    /**
     * Returns the {@link EntityManager} instance for managing/querying entities.
     */
    EntityManager getEntityManager();
    
    /**
     * Returns the {@link ExecutionManager} instance for entities and users in this management realm 
     * to submit tasks and to observe what tasks are occurring
     */
    ExecutionManager getExecutionManager();

    /**
     * Returns the {@link EntityDriverManager} entities can use to create drivers. This
     * manager can also be used to programmatically customize which driver type to use 
     * for entities in different locations.
     * 
     * The default strategy for choosing a driver is to use a naming convention: 
     * {@link DriverDependentEntity#getDriverInterface()} returns the interface that the
     * driver must implement; its name should end in "Driver". For example, this suffix is 
     * replaced with "SshDriver" for SshMachineLocation, for example.
     */
    EntityDriverManager getEntityDriverManager();

    /**
     * Returns the {@link DownloadResolverManager} for resolving things like which URL to download an installer from.
     * 
     * The default {@link DownloadResolverManager} will retrieve {@code entity.getAttribute(Attributes.DOWNLOAD_URL)},
     * and substitute things like "${version}" etc.
     * 
     * However, additional resolvers can be registered to customize this behaviour (e.g. to always go to an 
     * enterprise's repository).
     */
    DownloadResolverManager getEntityDownloadsManager();

    /**
     * Returns the {@link SubscriptionManager} instance for entities and users of this management realm
     * to subscribe to sensor events (and, in the case of entities, to emit sensor events) 
     */
    SubscriptionManager getSubscriptionManager();

    //TODO (Alex) I'm not sure the following two getXxxContext methods are needed on the interface;
    //I expect they will only be called once, in AbstractEntity, and fully capable
    //there of generating the respective contexts from the managers
    //(Litmus test will be whether there is anything in FederatedManagementContext
    //which requires a custom FederatedExecutionContext -- or whether BasicEC 
    //works with FederatedExecutionManager)
    /**
     * Returns an {@link ExecutionContext} instance representing tasks 
     * (from the {@link ExecutionManager}) associated with this entity, and capable 
     * of conveniently running such tasks which will be associated with that entity  
     */
    ExecutionContext getExecutionContext(Entity entity);
    
    /**
     * Returns a {@link SubscriptionContext} instance representing subscriptions
     * (from the {@link SubscriptionManager}) associated with this entity, and capable 
     * of conveniently subscribing on behalf of that entity  
     */
    SubscriptionContext getSubscriptionContext(Entity entity);

    RebindManager getRebindManager();
    
    /**
     * Returns the ConfigMap (e.g. BrooklynProperties) applicable to this management context.
     * Defaults to reading ~/.brooklyn/brooklyn.properties but configurable in the management context.
     */
    StringConfigMap getConfig();
    
    /**
     * Whether this management context is still running, or has been terminated.
     */
    public boolean isRunning();

    /** Record of configured locations and location resolvers */
    LocationRegistry getLocationRegistry();
    
    /** Record of configured Brooklyn entities (and templates and policies) which can be loaded */
    BrooklynCatalog getCatalog();

    LocationManager getLocationManager();

    /**
     * For controlling access to operations - can be queried to find if an operation is allowed.
     * Callers should *not* cache the result of this method, but should instead always call
     * again to get the {@link AccessController}.
     */
    AccessController getAccessController();

    /**
     * Reloads locations from brooklyn.properties. Any changes will apply only to newly created applications
     * @return 
     */
    void reloadBrooklynProperties();
}