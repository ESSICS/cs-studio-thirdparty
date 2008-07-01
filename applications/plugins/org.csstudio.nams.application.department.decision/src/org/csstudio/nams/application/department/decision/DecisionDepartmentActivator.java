/* 
 * Copyright (c) C1 WPS mbH, HAMBURG, GERMANY. All Rights Reserved.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR
 * PURPOSE AND  NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, 
 * REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
 * PART OF THIS LICENSE. NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER 
 * EXCEPT UNDER THIS DISCLAIMER.
 * C1 WPS HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 * ENHANCEMENTS, OR MODIFICATIONS. THE FULL LICENSE SPECIFYING FOR THE 
 * SOFTWARE THE REDISTRIBUTION, MODIFICATION, USAGE AND OTHER RIGHTS AND 
 * OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU 
 * MAY FIND A COPY AT
 * {@link http://www.eclipse.org/org/documents/epl-v10.html}.
 */
package org.csstudio.nams.application.department.decision;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.csstudio.nams.common.activatorUtils.AbstractBundleActivator;
import org.csstudio.nams.common.activatorUtils.OSGiBundleActivationMethod;
import org.csstudio.nams.common.activatorUtils.OSGiBundleDeactivationMethod;
import org.csstudio.nams.common.activatorUtils.OSGiService;
import org.csstudio.nams.common.activatorUtils.Required;
import org.csstudio.nams.common.decision.Eingangskorb;
import org.csstudio.nams.common.decision.StandardAblagekorb;
import org.csstudio.nams.common.decision.Vorgangsmappe;
import org.csstudio.nams.common.decision.Vorgangsmappenkennung;
import org.csstudio.nams.common.material.regelwerk.Regelwerk;
import org.csstudio.nams.common.material.regelwerk.WeiteresVersandVorgehen;
import org.csstudio.nams.common.service.ExecutionService;
import org.csstudio.nams.common.service.StepByStepProcessor;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.ConfigurationServiceFactory;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.LocalStoreConfigurationService;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.InconsistentConfigurationException;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.StorageException;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.UnknownConfigurationElementError;
import org.csstudio.nams.service.history.declaration.HistoryService;
import org.csstudio.nams.service.logging.declaration.Logger;
import org.csstudio.nams.service.messaging.declaration.Consumer;
import org.csstudio.nams.service.messaging.declaration.MessagingService;
import org.csstudio.nams.service.messaging.declaration.MessagingSession;
import org.csstudio.nams.service.messaging.declaration.MultiConsumersConsumer;
import org.csstudio.nams.service.messaging.declaration.NAMSMessage;
import org.csstudio.nams.service.messaging.declaration.PostfachArt;
import org.csstudio.nams.service.messaging.declaration.Producer;
import org.csstudio.nams.service.messaging.exceptions.MessagingException;
import org.csstudio.nams.service.preferenceservice.declaration.PreferenceService;
import org.csstudio.nams.service.preferenceservice.declaration.PreferenceServiceDatabaseKeys;
import org.csstudio.nams.service.preferenceservice.declaration.PreferenceServiceJMSKeys;
import org.csstudio.nams.service.regelwerkbuilder.declaration.RegelwerkBuilderService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleActivator;

import de.c1wps.desy.ams.alarmentscheidungsbuero.AlarmEntscheidungsBuero;

/**
 * <p>
 * The decision department or more precise the activator and application class
 * to controls their life cycle.
 * </p>
 * 
 * <p>
 * <strong>Pay attention:</strong> There are always exactly two instances of
 * this class present: The <emph>bundle activator instance</emph> and the
 * <emph>bundle application instance</emph>. The communication of both is
 * hidden in this class to hide the dirty static singleton communication. This
 * is required during the instantation of extensions (like {@link IApplication})
 * is done in the framework and not by the plug in itself like it should be.
 * Cause of this all service field filled by the <emph>bundles activator</emph>
 * start operation are static to be accessible from the <emph>bundles
 * application</emph> start.
 * </p>
 * 
 * @author <a href="mailto:mz@c1-wps.de">Matthias Zeimer</a>
 * @author <a href="mailto:gs@c1-wps.de">Goesta Steen</a>
 * 
 * @version 0.1-2008-04-25: Created.
 * @version 0.1.1-2008-04-28 (MZ): Change to use
 *          org.csstudio.nams.common.activatorUtils.BundleActivatorUtils.
 * @version 0.2.0-2008-06-10 (MZ): Change to use {@link AbstractBundleActivator}.
 */
public class DecisionDepartmentActivator extends AbstractBundleActivator
		implements IApplication, BundleActivator {

	/**
	 * The plug-in ID of this bundle.
	 */
	public static final String PLUGIN_ID = "org.csstudio.nams.application.department.decision";

	/**
	 * Gemeinsames Attribut des Activators und der Application: Der Logger.
	 */
	private static Logger logger;

	/**
	 * Gemeinsames Attribut des Activators und der Application: Fatory for
	 * creating Consumers
	 */
	private static MessagingService messagingService;

	/**
	 * Indicates if the application instance should continue working. Unused in
	 * the activator instance.
	 * 
	 * This field is set by another thread to indicate that application should
	 * shut down.
	 */
	private volatile boolean _continueWorking;

	/**
	 * Referenz auf den Thread, welcher die JMS Nachrichten anfragt. Wird
	 * genutzt um den Thread zu "interrupten". Wird nur von der Application
	 * benutzt.
	 */
	private Thread _receiverThread;

	/**
	 * Service für das Entscheidungsbüro um das starten der asynchronen
	 * Ausführung von Einzelaufgaben (Threads) zu kapseln.
	 */
	private static ExecutionService executionService;

	private static PreferenceService preferenceService;

	private static RegelwerkBuilderService regelwerkBuilderService;

	private static HistoryService historyService;

	private MessagingSession amsMessagingSessionForConsumer;

	/**
	 * Consumer zum Lesen auf Alarmnachrichten-Quelle.
	 */
	private Consumer extAlarmConsumer;

	/**
	 * Consumer zum Lesen auf externer-Komando-Quelle.
	 */
	private Consumer extCommandConsumer;

	/**
	 * Consumer zum Lesen auf ams-Komando-Quelle.
	 */
	private Consumer amsCommandConsumer;

	/**
	 * Producer zum Senden auf ams-Zielablage (normally Distributor or
	 * MessageMinder).
	 */
	private Producer amsAusgangsProducer;

	/**
	 * Service to receive configuration-data. Used by
	 * {@link RegelwerkBuilderService}.
	 */
	private static LocalStoreConfigurationService localStoreConfigurationService;

	/**
	 * MessageSession für externe Quellen und Ziele.
	 */
	private MessagingSession extMessagingSessionForConsumer;

	/**
	 * MessageSession für ams interne Quellen und Ziele.
	 */
	private MessagingSession amsMessagingSessionForProducer;

	private AlarmEntscheidungsBuero _alarmEntscheidungsBuero;

	private StepByStepProcessor _ausgangskorbBearbeiter;

	private Integer _applicationExitStatus = IApplication.EXIT_OK;

	// private AbstractMultiConsumerMessageHandler
	// messageHandlerToRecieveUntilApplicationQuits;

	private Eingangskorb<Vorgangsmappe> eingangskorbDesDecisionOffice;

	private StandardAblagekorb<Vorgangsmappe> ausgangskorbDesDecisionOfficeUndEingangskorbDesPostOffice;

	/**
	 * Starts the bundle activator instance. First Step.
	 * 
	 * @see BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@OSGiBundleActivationMethod
	public void startBundle(@OSGiService
	@Required
	final Logger injectedLogger, @OSGiService
	@Required
	final MessagingService injectedMessagingService, @OSGiService
	@Required
	final PreferenceService injectedPreferenceService, @OSGiService
	@Required
	final RegelwerkBuilderService injectedBuilderService, @OSGiService
	@Required
	final HistoryService injectedHistoryService, @OSGiService
	@Required
	final ConfigurationServiceFactory injectedConfigurationServiceFactory,
			@OSGiService
			@Required
			final ExecutionService injectedExecutionService) throws Exception {

		// ** Services holen...

		// Logging Service
		DecisionDepartmentActivator.logger = injectedLogger;

		DecisionDepartmentActivator.logger.logInfoMessage(this, "plugin "
				+ DecisionDepartmentActivator.PLUGIN_ID
				+ " initializing Services");

		// Messaging Service
		DecisionDepartmentActivator.messagingService = injectedMessagingService;

		// Preference Service (wird als konfiguration verwendet!!)
		DecisionDepartmentActivator.preferenceService = injectedPreferenceService;

		// RegelwerkBuilder Service
		DecisionDepartmentActivator.regelwerkBuilderService = injectedBuilderService;

		// History Service
		DecisionDepartmentActivator.historyService = injectedHistoryService;

		// LocalStoreConfigurationService
		DecisionDepartmentActivator.localStoreConfigurationService = injectedConfigurationServiceFactory
				.getConfigurationService(
						"org.apache.derby.jdbc.ClientDriver",
						DecisionDepartmentActivator.preferenceService
								.getString(PreferenceServiceDatabaseKeys.P_APP_DATABASE_CONNECTION),
						"org.hibernate.dialect.DerbyDialect",
						DecisionDepartmentActivator.preferenceService
								.getString(PreferenceServiceDatabaseKeys.P_APP_DATABASE_USER),
						DecisionDepartmentActivator.preferenceService
								.getString(PreferenceServiceDatabaseKeys.P_APP_DATABASE_PASSWORD));

		// Execution Service
		// TODO wird noch nicht vollstaendig benutzt! Ins Dec-Office einbauen
		DecisionDepartmentActivator.executionService = injectedExecutionService;

		DecisionDepartmentActivator.logger.logInfoMessage(this, "plugin "
				+ DecisionDepartmentActivator.PLUGIN_ID
				+ " started succesfully.");
	}

	/**
	 * Stops the bundle activator instance. Last Step.
	 * 
	 * @see BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@OSGiBundleDeactivationMethod
	public void stopBundle(@OSGiService
	@Required
	final Logger logger) throws Exception {
		logger.logInfoMessage(this, "Plugin "
				+ DecisionDepartmentActivator.PLUGIN_ID
				+ " stopped succesfully.");
	}

	/**
	 * Starts the bundle application instance. Second Step.
	 * 
	 * @see IApplication#start(IApplicationContext)
	 */
	public Object start(final IApplicationContext context) {
		this._receiverThread = Thread.currentThread();
		this._alarmEntscheidungsBuero = null;
		this._ausgangskorbBearbeiter = null;
		this._continueWorking = true;

		DecisionDepartmentActivator.logger
				.logInfoMessage(this,
						"Decision department application is going to be initialized...");

		DecisionDepartmentActivator.logger
				.logInfoMessage(this,
						"Decision department application is configuring execution service...");
		this
				.initialisiereThredGroupTypes(DecisionDepartmentActivator.executionService);

		try {
			DecisionDepartmentActivator.logger.logInfoMessage(this,
					"Decision department application is creating consumers...");

			final String amsProvider1 = DecisionDepartmentActivator.preferenceService
					.getString(PreferenceServiceJMSKeys.P_JMS_AMS_PROVIDER_URL_1);
			final String amsProvider2 = DecisionDepartmentActivator.preferenceService
					.getString(PreferenceServiceJMSKeys.P_JMS_AMS_PROVIDER_URL_2);

			DecisionDepartmentActivator.logger.logDebugMessage(this,
					"PreferenceServiceJMSKeys.P_JMS_AMS_PROVIDER_URL_1 = "
							+ amsProvider1);
			DecisionDepartmentActivator.logger.logDebugMessage(this,
					"PreferenceServiceJMSKeys.P_JMS_AMS_PROVIDER_URL_2 = "
							+ amsProvider2);

			// FIXME clientid!! gegebenenfalls aus preferencestore holen
			this.amsMessagingSessionForConsumer = DecisionDepartmentActivator.messagingService
					.createNewMessagingSession("amsConsumer", new String[] {
							amsProvider1, amsProvider2 });
			this.extMessagingSessionForConsumer = DecisionDepartmentActivator.messagingService
					.createNewMessagingSession(
							"extConsumer",
							new String[] {
									DecisionDepartmentActivator.preferenceService
											.getString(PreferenceServiceJMSKeys.P_JMS_EXTERN_PROVIDER_URL_1),
									DecisionDepartmentActivator.preferenceService
											.getString(PreferenceServiceJMSKeys.P_JMS_EXTERN_PROVIDER_URL_2) });

			this.extAlarmConsumer = this.extMessagingSessionForConsumer
					.createConsumer(
							DecisionDepartmentActivator.preferenceService
									.getString(PreferenceServiceJMSKeys.P_JMS_EXT_TOPIC_ALARM),
							PostfachArt.TOPIC);
			this.extCommandConsumer = this.extMessagingSessionForConsumer
					.createConsumer(
							DecisionDepartmentActivator.preferenceService
									.getString(PreferenceServiceJMSKeys.P_JMS_EXT_TOPIC_COMMAND),
							PostfachArt.TOPIC);
			this.amsCommandConsumer = this.amsMessagingSessionForConsumer
					.createConsumer(
							DecisionDepartmentActivator.preferenceService
									.getString(PreferenceServiceJMSKeys.P_JMS_AMS_TOPIC_COMMAND),
							PostfachArt.TOPIC);
			DecisionDepartmentActivator.logger.logInfoMessage(this,
					"Decision department application is creating producers...");

			// FIXME clientid!!
			this.amsMessagingSessionForProducer = DecisionDepartmentActivator.messagingService
					.createNewMessagingSession(
							"amsProducer",
							new String[] { DecisionDepartmentActivator.preferenceService
									.getString(PreferenceServiceJMSKeys.P_JMS_AMS_SENDER_PROVIDER_URL) });

			this.amsAusgangsProducer = this.amsMessagingSessionForProducer
					.createProducer(
							DecisionDepartmentActivator.preferenceService
									.getString(PreferenceServiceJMSKeys.P_JMS_AMS_TOPIC_MESSAGEMINDER),
							PostfachArt.TOPIC);

		} catch (final Throwable e) {
			DecisionDepartmentActivator.logger
					.logFatalMessage(
							this,
							"Exception while initializing the alarm decision department.",
							e);
			this._continueWorking = false;
		}

		/*-
		 * Vor der naechsten Zeile darf niemals ein Zugriff auf die lokale
		 * Cofigurations-DB (application-DB) erfolgen, da zuvor dort noch
		 * keine validen Daten liegen. Der folgende Aufruf blockiert
		 * solange, bis der Distributor bestaetigt, dass die Synchronisation
		 * erfolgreich ausgefuehrt wurde.
		 */
		this._continueWorking = DecisionDepartmentActivator
				.versucheZuSynchronisieren(
						this,
						DecisionDepartmentActivator.logger,
						this.amsAusgangsProducer,
						this.amsCommandConsumer,
						DecisionDepartmentActivator.localStoreConfigurationService);

		if (this._continueWorking) {
			try {
				DecisionDepartmentActivator.logger
						.logInfoMessage(this,
								"Decision department application is creating decision office...");

				final List<Regelwerk> alleRegelwerke = DecisionDepartmentActivator.regelwerkBuilderService
						.gibAlleRegelwerke();

				DecisionDepartmentActivator.logger.logDebugMessage(this,
						"alleRegelwerke size: " + alleRegelwerke.size());
				for (final Regelwerk regelwerk : alleRegelwerke) {
					DecisionDepartmentActivator.logger.logDebugMessage(this,
							regelwerk.toString());
				}

				this.eingangskorbDesDecisionOffice = new StandardAblagekorb<Vorgangsmappe>();
				this.ausgangskorbDesDecisionOfficeUndEingangskorbDesPostOffice = new StandardAblagekorb<Vorgangsmappe>();

				this._alarmEntscheidungsBuero = new AlarmEntscheidungsBuero(
						alleRegelwerke.toArray(new Regelwerk[alleRegelwerke
								.size()]),
						this.eingangskorbDesDecisionOffice,
						this.ausgangskorbDesDecisionOfficeUndEingangskorbDesPostOffice);
			} catch (final Throwable e) {
				DecisionDepartmentActivator.logger
						.logFatalMessage(
								this,
								"Exception while initializing the alarm decision department.",
								e);
				this._continueWorking = false;
			}
		}

		if (this._continueWorking) {
			DecisionDepartmentActivator.logger
					.logInfoMessage(
							this,
							"******* Decision department application successfully initialized, beginning work... *******");

			// Ausgangskoerbe nebenläufig abfragen
			this._ausgangskorbBearbeiter = new AusgangsKorbBearbeiter(
					this.ausgangskorbDesDecisionOfficeUndEingangskorbDesPostOffice);

			DecisionDepartmentActivator.executionService.executeAsynchronsly(
					ThreadTypesOfDecisionDepartment.AUSGANGSKORBBEARBEITER,
					this._ausgangskorbBearbeiter);

			// start receiving Messages, runs while _continueWorking is true.
			this
					.receiveMessagesUntilApplicationQuits(this.eingangskorbDesDecisionOffice);
		}
		DecisionDepartmentActivator.logger
				.logInfoMessage(
						this,
						"Decision department has stopped message processing and continue shutting down...");

		if (this._alarmEntscheidungsBuero != null) {
			this._alarmEntscheidungsBuero
					.beendeArbeitUndSendeSofortAlleOffeneneVorgaenge();
		}

		// Warte auf Thread für Ausgangskorb-Bearbeitung
		if ((this._ausgangskorbBearbeiter != null)
				&& this._ausgangskorbBearbeiter.isCurrentlyRunning()) {
			// FIXME Warte bis korb leer ist.
			this._ausgangskorbBearbeiter.stopWorking();
		}

		// Alle Verbindungen schließen
		DecisionDepartmentActivator.logger
				.logInfoMessage(this,
						"Decision department application is closing opened connections...");
		if (this.amsAusgangsProducer != null) {
			this.amsAusgangsProducer.close();
		}
		if (this.amsCommandConsumer != null) {
			this.amsCommandConsumer.close();
		}
		if (this.amsMessagingSessionForConsumer != null) {
			this.amsMessagingSessionForConsumer.close();
		}
		if (this.amsMessagingSessionForProducer != null) {
			this.amsMessagingSessionForProducer.close();
		}
		if (this.extAlarmConsumer != null) {
			this.extAlarmConsumer.close();
		}
		if (this.extCommandConsumer != null) {
			this.extCommandConsumer.close();
		}
		if (this.extMessagingSessionForConsumer != null) {
			this.extMessagingSessionForConsumer.close();
		}

		DecisionDepartmentActivator.logger.logInfoMessage(this,
				"Decision department application successfully shuted down.");
		return this._applicationExitStatus;
	}

	/**
	 * Versucht via dem Distributor eine Synchronisation auszufürehn. Das
	 * Ergebnis gibt an, ob weitergearbeitet werden soll.
	 * 
	 * @param instance
	 * @param logger
	 * @param amsAusgangsProducer
	 * @param amsCommandConsumer
	 * @param localStoreConfigurationService
	 * @return {@code true} bei Erfolg, {@false} sonst.
	 */
	private static boolean versucheZuSynchronisieren(
			final DecisionDepartmentActivator instance, final Logger logger,
			final Producer amsAusgangsProducer,
			final Consumer amsCommandConsumer,
			final LocalStoreConfigurationService localStoreConfigurationService) {
		boolean result = false;
		try {

			logger
					.logInfoMessage(
							instance,
							"Decision department application orders distributor to synchronize configuration...");
			SyncronisationsAutomat.syncronisationUeberDistributorAusfueren(
					amsAusgangsProducer, amsCommandConsumer,
					localStoreConfigurationService,
					DecisionDepartmentActivator.historyService);
			if (!SyncronisationsAutomat.hasBeenCanceled()) {
				// Abbruch bei Syncrinisation
				result = true;
			}
		} catch (final MessagingException messagingException) {
			if (SyncronisationsAutomat.hasBeenCanceled()) {
				// Abbruch bei Syncrinisation
				logger
						.logInfoMessage(
								instance,
								"Decision department application was interrupted and requested to shut down during synchroisation of configuration.");

			} else {

				logger.logFatalMessage(instance,
						"Exception while synchronizing configuration.",
						messagingException);
				result = false;

			}
		} catch (final StorageException storageException) {
			logger.logFatalMessage(instance,
					"Exception while synchronizing configuration.",
					storageException);
			result = false;
		} catch (final UnknownConfigurationElementError unknownConfigurationElementError) {
			logger.logFatalMessage(instance,
					"Exception while synchronizing configuration.",
					unknownConfigurationElementError);
			result = false;
		} catch (final InconsistentConfigurationException inconsistentConfiguration) {
			logger.logFatalMessage(instance,
					"Exception while synchronizing configuration.",
					inconsistentConfiguration);
			result = false;
		}
		return result;
	}

	private void initialisiereThredGroupTypes(
			final ExecutionService executionServiceToBeInitialize) {
		executionServiceToBeInitialize
				.registerGroup(
						ThreadTypesOfDecisionDepartment.ABTEILUNGSLEITER,
						new ThreadGroup(
								ThreadTypesOfDecisionDepartment.ABTEILUNGSLEITER
										.name()));
		executionServiceToBeInitialize.registerGroup(
				ThreadTypesOfDecisionDepartment.AUSGANGSKORBBEARBEITER,
				new ThreadGroup(
						ThreadTypesOfDecisionDepartment.AUSGANGSKORBBEARBEITER
								.name()));
		// executionServiceToBeInitialize
		// .registerGroup(
		// AbstractMultiConsumerMessageHandler.MultiConsumerMessageThreads.CONSUMER_THREAD,
		// new ThreadGroup(
		// AbstractMultiConsumerMessageHandler.MultiConsumerMessageThreads.CONSUMER_THREAD
		// .name()));
		// executionServiceToBeInitialize
		// .registerGroup(
		// AbstractMultiConsumerMessageHandler.MultiConsumerMessageThreads.HANDLER_THREAD,
		// new ThreadGroup(
		// AbstractMultiConsumerMessageHandler.MultiConsumerMessageThreads.HANDLER_THREAD
		// .name()));
		executionServiceToBeInitialize
				.registerGroup(
						MultiConsumersConsumer.MultiConsumerConsumerThreads.CONSUMER_THREAD,
						new ThreadGroup(
								MultiConsumersConsumer.MultiConsumerConsumerThreads.CONSUMER_THREAD
										.name()));
		// TODO Register remaining types!
	}

	/**
	 * This method is receiving Messages and handle them. It will block until
	 * _continueWorking get false.
	 * 
	 * @param eingangskorb
	 *            Der {@link Eingangskorb} to read on.
	 */
	private void receiveMessagesUntilApplicationQuits(
			final Eingangskorb<Vorgangsmappe> eingangskorb) {

		final Consumer[] consumerArray = new Consumer[] {
				this.amsCommandConsumer, this.extAlarmConsumer,
				this.extCommandConsumer };

		final MultiConsumersConsumer consumersConsumer = new MultiConsumersConsumer(
				DecisionDepartmentActivator.logger, consumerArray,
				DecisionDepartmentActivator.executionService);

		while (this._continueWorking) {
			try {
				final NAMSMessage message = consumersConsumer.receiveMessage();
				try {
					DecisionDepartmentActivator.logger.logInfoMessage(this,
							"Decision department recieves a message to handle: "
									+ message.toString());
					if (message.enthaeltAlarmnachricht()) {
						try {
							eingangskorb.ablegen(new Vorgangsmappe(
									Vorgangsmappenkennung.createNew(/**
									 * TODO Host
									 * Service
									 * statt new
									 * InetAddress()
									 * .getLocalHost
									 * benutzen
									 */
									InetAddress.getLocalHost(), /**
									 * TODO Calender
									 * Service statt
									 * new Date()
									 * benutzen
									 */
									new Date()), message.alsAlarmnachricht()));
						} catch (final UnknownHostException e) {
							DecisionDepartmentActivator.logger.logFatalMessage(
									this, "Host unreachable", e);
						} catch (final InterruptedException e) {
							DecisionDepartmentActivator.logger.logInfoMessage(
									this, "Message processing interrupted", e);
						}
					} else if (message.enthaeltSystemnachricht()) {
						if (message.alsSystemachricht()
								.istSyncronisationsAufforderung()) {
							DecisionDepartmentActivator.historyService
									.logReceivedStartReplicationMessage();
							this._applicationExitStatus = IApplication.EXIT_RESTART;
							/*-
							 * Vollständiges runterfahren zur Aktualisierung
							 * vermeidet das Vergessen von Teilsystemen. Während
							 * einer Aktualisierung können ohnehin keine
							 * Alarmnachrichten nebenläufig verarbeitet werden.
							 */
						}
					}
				} finally {
					try {
						message.acknowledge();
						if (this._applicationExitStatus == IApplication.EXIT_RESTART) {
							this.stop();
						}
					} catch (final MessagingException e) {
						DecisionDepartmentActivator.logger.logWarningMessage(
								this, "unable to ackknowlwedge message: "
										+ message.toString(), e);
					}
				}
			} catch (final MessagingException e) {
				// TODO was soll hier geschehen?
				DecisionDepartmentActivator.logger.logErrorMessage(this,
						"Exception during recieve of message.", e);
			} catch (final InterruptedException ie) {
				DecisionDepartmentActivator.logger.logInfoMessage(this,
						"Recieving of message has benn interrupted", ie);
			}
		}

		consumersConsumer.close();
	}

	/**
	 * Stops the bundle application instance.Ppenultimate Step.
	 * 
	 * @see IApplication#start(IApplicationContext)
	 */
	public void stop() {
		DecisionDepartmentActivator.logger
				.logInfoMessage(this,
						"Start to shut down decision department application on user request...");
		this._continueWorking = false;
		if (SyncronisationsAutomat.isRunning()) {
			DecisionDepartmentActivator.logger.logInfoMessage(this,
					"Canceling running syncronisation...");
			SyncronisationsAutomat.cancel();
		}

		DecisionDepartmentActivator.logger.logInfoMessage(this,
				"Interrupting working thread...");
		this._receiverThread.interrupt();
	}

	class AusgangsKorbBearbeiter extends StepByStepProcessor {

		private final Eingangskorb<Vorgangsmappe> vorgangskorb;

		public AusgangsKorbBearbeiter(
				final Eingangskorb<Vorgangsmappe> vorgangskorb) {
			this.vorgangskorb = vorgangskorb;
		}

		@Override
		protected void doRunOneSingleStep() throws Throwable {

			try {
				final Vorgangsmappe vorgangsmappe = this.vorgangskorb
						.entnehmeAeltestenEingang();
				if (vorgangsmappe.istAbgeschlossenDurchTimeOut()) {
					DecisionDepartmentActivator.historyService
							.logTimeOutForTimeBased(vorgangsmappe);
				}
				DecisionDepartmentActivator.logger.logDebugMessage(this,
						"gesamtErgebnis: "
								+ vorgangsmappe.gibPruefliste()
										.gesamtErgebnis());

				if (vorgangsmappe.gibPruefliste().gesamtErgebnis() == WeiteresVersandVorgehen.VERSENDEN) {
					// Nachricht nicht anreichern. Wird im JMSProducer
					// gemacht
					// Versenden
					DecisionDepartmentActivator.this.amsAusgangsProducer
							.sendeVorgangsmappe(vorgangsmappe);
				}

			} catch (final InterruptedException e) {
				// wird zum stoppen benötigt.
				// hier muss nichts unternommen werden
			}
		}
	};
}
