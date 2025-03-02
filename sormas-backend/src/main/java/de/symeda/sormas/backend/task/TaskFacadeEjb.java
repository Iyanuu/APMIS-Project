/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.backend.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.caze.BirthDateDto;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.messaging.MessageType;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.task.TaskCriteria;
import de.symeda.sormas.api.task.TaskDto;
import de.symeda.sormas.api.task.TaskExportDto;
import de.symeda.sormas.api.task.TaskFacade;
import de.symeda.sormas.api.task.TaskIndexDto;
import de.symeda.sormas.api.task.TaskJurisdictionFlagsDto;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.travelentry.TravelEntryReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.caze.CaseQueryContext;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.ConfigFacadeEjb.ConfigFacadeEjbLocal;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.common.CronService;
import de.symeda.sormas.backend.common.messaging.MessageSubject;
import de.symeda.sormas.backend.common.messaging.MessagingService;
import de.symeda.sormas.backend.common.messaging.NotificationDeliveryFailedException;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactFacadeEjb;
import de.symeda.sormas.backend.contact.ContactQueryContext;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventFacadeEjb;
import de.symeda.sormas.backend.event.EventService;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.location.LocationJoins;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.travelentry.TravelEntry;
import de.symeda.sormas.backend.travelentry.TravelEntryFacadeEjb;
import de.symeda.sormas.backend.travelentry.TravelEntryService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.Pseudonymizer;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "TaskFacade")
public class TaskFacadeEjb implements TaskFacade {

	private static final int ARCHIVE_BATCH_SIZE = 1000;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private TaskService taskService;
	@EJB
	private UserService userService;
	@EJB
	private CaseService caseService;
	@EJB
	private ContactService contactService;
	@EJB
	private EventService eventService;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private MessagingService messagingService;
	@EJB
	private ConfigFacadeEjbLocal configFacade;
	@EJB
	private TravelEntryService travelEntryService;

	public Task fromDto(TaskDto source, boolean checkChangeDate) {

		if (source == null) {
			return null;
		}

		Task target = DtoHelper.fillOrBuildEntity(source, taskService.getByUuid(source.getUuid()), Task::new, checkChangeDate);

		target.setAssigneeUser(userService.getByReferenceDto(source.getAssigneeUser()));
		target.setAssigneeReply(source.getAssigneeReply());
		target.setCreatorUser(userService.getByReferenceDto(source.getCreatorUser()));
		target.setCreatorComment(source.getCreatorComment());
		target.setPriority(source.getPriority());
		target.setDueDate(source.getDueDate());
		target.setSuggestedStart(source.getSuggestedStart());
		target.setPerceivedStart(source.getPerceivedStart());
		// TODO is this a good place to do this?
		if (target.getTaskStatus() != source.getTaskStatus()) {
			target.setStatusChangeDate(new Date());
		} else {
			target.setStatusChangeDate(source.getStatusChangeDate());
		}
		target.setTaskStatus(source.getTaskStatus());
		target.setTaskType(source.getTaskType());

		target.setClosedLat(source.getClosedLat());
		target.setClosedLon(source.getClosedLon());
		target.setClosedLatLonAccuracy(source.getClosedLatLonAccuracy());

		target.setTaskContext(source.getTaskContext());
		if (source.getTaskContext() != null) {
			switch (source.getTaskContext()) {
			case CASE:
				target.setCaze(caseService.getByReferenceDto(source.getCaze()));
				target.setContact(null);
				target.setEvent(null);
				target.setTravelEntry(null);
				break;
			case CONTACT:
				target.setCaze(null);
				target.setContact(contactService.getByReferenceDto(source.getContact()));
				target.setEvent(null);
				target.setTravelEntry(null);
				break;
			case EVENT:
				target.setCaze(null);
				target.setContact(null);
				target.setEvent(eventService.getByReferenceDto(source.getEvent()));
				target.setTravelEntry(null);
				break;
			case TRAVEL_ENTRY:
				target.setCaze(null);
				target.setContact(null);
				target.setEvent(null);
				target.setTravelEntry(travelEntryService.getByReferenceDto(source.getTravelEntry()));
				break;
			case GENERAL:
				target.setCaze(null);
				target.setContact(null);
				target.setEvent(null);
				target.setTravelEntry(null);
				break;
			default:
				throw new UnsupportedOperationException(source.getTaskContext() + " is not implemented");
			}
		} else {
			target.setCaze(null);
			target.setContact(null);
			target.setEvent(null);
		}

		return target;
	}

	public TaskDto toDto(Task source, Pseudonymizer pseudonymizer) {

		if (source == null) {
			return null;
		}

		TaskDto target = new TaskDto();

		DtoHelper.fillDto(target, source);

		target.setAssigneeUser(UserFacadeEjb.toReferenceDto(source.getAssigneeUser()));
		target.setAssigneeReply(source.getAssigneeReply());
		target.setCreatorUser(UserFacadeEjb.toReferenceDto(source.getCreatorUser()));
		target.setCreatorComment(source.getCreatorComment());
		target.setPriority(source.getPriority());
		target.setDueDate(source.getDueDate());
		target.setSuggestedStart(source.getSuggestedStart());
		target.setPerceivedStart(source.getPerceivedStart());
		target.setStatusChangeDate(source.getStatusChangeDate());
		target.setTaskContext(source.getTaskContext());
		target.setTaskStatus(source.getTaskStatus());
		target.setTaskType(source.getTaskType());
		target.setCaze(CaseFacadeEjb.toReferenceDto(source.getCaze()));
		target.setContact(ContactFacadeEjb.toReferenceDto(source.getContact()));
		target.setEvent(EventFacadeEjb.toReferenceDto(source.getEvent()));
		target.setTravelEntry(TravelEntryFacadeEjb.toReferenceDto(source.getTravelEntry()));

		target.setClosedLat(source.getClosedLat());
		target.setClosedLon(source.getClosedLon());
		target.setClosedLatLonAccuracy(source.getClosedLatLonAccuracy());

		TaskJurisdictionFlagsDto taskJurisdictionFlagsDto = taskService.inJurisdictionOrOwned(source);
		pseudonymizer.pseudonymizeDto(TaskDto.class, target, taskJurisdictionFlagsDto.getInJurisdiction(), t -> {
			if (source.getCaze() != null) {
				pseudonymizer.pseudonymizeDto(CaseReferenceDto.class, target.getCaze(), taskJurisdictionFlagsDto.getCaseInJurisdiction(), null);
			}

			if (source.getContact() != null) {
				pseudonymizeContactReference(
					pseudonymizer,
					target.getContact(),
					taskJurisdictionFlagsDto.getContactInJurisdiction(),
					taskJurisdictionFlagsDto.getContactCaseInJurisdiction());
			}

			if (source.getEvent() != null) {
				pseudonymizer.pseudonymizeDto(EventReferenceDto.class, target.getEvent(), taskJurisdictionFlagsDto.getEventInJurisdiction(), null);
			}

			if (source.getTravelEntry() != null) {
				pseudonymizer.pseudonymizeDto(
					TravelEntryReferenceDto.class,
					target.getTravelEntry(),
					taskJurisdictionFlagsDto.getTravelEntryInJurisdiction(),
					null);
			}
		});

		return target;
	}

	@Override
	public TaskDto saveTask(@Valid TaskDto dto) {

		Task ado = fromDto(dto, true);

		validate(dto);

		taskService.ensurePersisted(ado);

		// once we have to handle additional logic this should be moved to it's own function or even class 
		if (ado.getTaskType() == TaskType.CASE_INVESTIGATION && ado.getCaze() != null) {
			caseFacade.updateInvestigationByTask(ado.getCaze());
		}

		if (ado.getTaskType() == TaskType.CONTACT_FOLLOW_UP && ado.getTaskStatus() == TaskStatus.DONE && ado.getContact() != null) {
			List<User> messageRecipients = userService.getAllByRegionsAndUserRoles(
				JurisdictionHelper.getContactRegions(ado.getContact()),
				UserRole.SURVEILLANCE_SUPERVISOR,
				UserRole.CASE_SUPERVISOR,
				UserRole.CONTACT_SUPERVISOR);
			for (User recipient : messageRecipients) {
				try {
					messagingService.sendMessage(
						recipient,
						MessageSubject.VISIT_COMPLETED,
						String.format(
							I18nProperties.getString(MessagingService.CONTENT_VISIT_COMPLETED),
							DataHelper.getShortUuid(ado.getContact().getUuid()),
							DataHelper.getShortUuid(ado.getAssigneeUser().getUuid())),
						MessageType.EMAIL,
						MessageType.SMS);
				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"NotificationDeliveryFailedException when trying to notify supervisors about the completion of a follow-up visit. "
								+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
							recipient.getUuid()));
				}
			}
		}

		return toDto(ado, Pseudonymizer.getDefault(userService::hasRight));
	}

	@Override
	public List<String> getAllActiveUuids() {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		return taskService.getAllActiveUuids(user);
	}

	@Override
	public List<TaskDto> getAllActiveTasksAfter(Date date) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return taskService.getAllActiveTasksAfter(date, user).stream().map(c -> toDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public Page<TaskIndexDto> getIndexPage(TaskCriteria taskCriteria, Integer offset, Integer size, List<SortProperty> sortProperties) {
		List<TaskIndexDto> taskIndexList = getIndexList(taskCriteria, offset, size, sortProperties);
		long totalElementCount = count(taskCriteria);
		return new Page<TaskIndexDto>(taskIndexList, offset, size, totalElementCount);
	}

	@Override
	public long count(TaskCriteria taskCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Task> task = cq.from(Task.class);
		TaskJoins joins = new TaskJoins(task);

		Predicate filter = null;
		if (taskCriteria == null || !taskCriteria.hasContextCriteria()) {
			filter = taskService.createUserFilter(cb, cq, task);
		} else {
			filter = CriteriaBuilderHelper.and(cb, filter, taskService.createAssigneeFilter(cb, joins.getAssignee()));
		}

		if (taskCriteria != null) {
			Predicate criteriaFilter = taskService.buildCriteriaFilter(taskCriteria, cb, task, joins);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.countDistinct(task));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<TaskIndexDto> getIndexList(TaskCriteria taskCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TaskIndexDto> cq = cb.createQuery(TaskIndexDto.class);
		Root<Task> task = cq.from(Task.class);

		TaskQueryContext taskQueryContext = new TaskQueryContext(cb, cq, task);
		TaskJoins<Task> joins = (TaskJoins<Task>) taskQueryContext.getJoins();

		// Filter select based on case/contact/event region/district/community
		Expression<Object> region = cb.selectCase()
			.when(cb.isNotNull(joins.getCaseRegion()), joins.getCaseRegion().get(Region.NAME))
			.otherwise(
				cb.selectCase()
					.when(cb.isNotNull(joins.getContactRegion()), joins.getContactRegion().get(Region.NAME))
					.otherwise(
						cb.selectCase()
							.when(cb.isNotNull(joins.getEventRegion()), joins.getEventRegion().get(Region.NAME))
							.otherwise(joins.getTravelEntryResponsibleRegion().get(Region.NAME))));

		Expression<Object> district = cb.selectCase()
			.when(cb.isNotNull(joins.getCaseDistrict()), joins.getCaseDistrict().get(District.NAME))
			.otherwise(
				cb.selectCase()
					.when(cb.isNotNull(joins.getContactDistrict()), joins.getContactDistrict().get(District.NAME))
					.otherwise(
						cb.selectCase()
							.when(cb.isNotNull(joins.getEventDistrict()), joins.getEventDistrict().get(District.NAME))
							.otherwise(joins.getTravelEntryResponsibleDistrict().get(District.NAME))));

		Expression<Object> community = cb.selectCase()
			.when(cb.isNotNull(joins.getCaseCommunity()), joins.getCaseCommunity().get(Community.NAME))
			.otherwise(
				cb.selectCase()
					.when(cb.isNotNull(joins.getContactCommunity()), joins.getContactCommunity().get(Community.NAME))
					.otherwise(
						cb.selectCase()
							.when(cb.isNotNull(joins.getEventCommunity()), joins.getEventCommunity().get(Community.NAME))
							.otherwise(joins.getTravelEntryResponsibleCommunity().get(Community.NAME))));

		List<Selection<?>> selections = new ArrayList<>(
			Arrays.asList(
				task.get(Task.UUID),
				task.get(Task.TASK_CONTEXT),
				joins.getCaze().get(Case.UUID),
				joins.getCasePerson().get(Person.FIRST_NAME),
				joins.getCasePerson().get(Person.LAST_NAME),
				joins.getEvent().get(Event.UUID),
				joins.getEvent().get(Event.EVENT_TITLE),
				joins.getEvent().get(Event.DISEASE),
				joins.getEvent().get(Event.DISEASE_DETAILS),
				joins.getEvent().get(Event.EVENT_STATUS),
				joins.getEvent().get(Event.EVENT_INVESTIGATION_STATUS),
				joins.getEvent().get(Event.START_DATE),
				joins.getContact().get(Contact.UUID),
				joins.getContactPerson().get(Person.FIRST_NAME),
				joins.getContactPerson().get(Person.LAST_NAME),
				joins.getContactCasePerson().get(Person.FIRST_NAME),
				joins.getContactCasePerson().get(Person.LAST_NAME),
				joins.getTravelEntry().get(TravelEntry.UUID),
				joins.getTravelEntry().get(TravelEntry.EXTERNAL_ID),
				joins.getTravelEntryPerson().get(Person.FIRST_NAME),
				joins.getTravelEntryPerson().get(Person.LAST_NAME),
				task.get(Task.TASK_TYPE),
				task.get(Task.PRIORITY),
				task.get(Task.DUE_DATE),
				task.get(Task.SUGGESTED_START),
				task.get(Task.TASK_STATUS),
				joins.getCreator().get(User.UUID),
				joins.getCreator().get(User.FIRST_NAME),
				joins.getCreator().get(User.LAST_NAME),
				task.get(Task.CREATOR_COMMENT),
				joins.getAssignee().get(User.UUID),
				joins.getAssignee().get(User.FIRST_NAME),
				joins.getAssignee().get(User.LAST_NAME),
				task.get(Task.ASSIGNEE_REPLY),
				region,
				district,
				community));

		selections.addAll(taskService.getJurisdictionSelections(taskQueryContext));
		cq.multiselect(selections);

		Predicate filter = null;
		if (taskCriteria == null || !taskCriteria.hasContextCriteria()) {
			filter = taskService.createUserFilter(cb, cq, task);
		} else {
			filter = CriteriaBuilderHelper.and(cb, filter, taskService.createAssigneeFilter(cb, joins.getAssignee()));
		}

		if (taskCriteria != null) {
			Predicate criteriaFilter = taskService.buildCriteriaFilter(taskCriteria, cb, task, joins);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		// Distinct is necessary here to avoid duplicate results due to the user role join in taskService.createAssigneeFilter
		cq.distinct(true);

		List<Order> order = new ArrayList<>();
		if (sortProperties != null && sortProperties.size() > 0) {
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case TaskIndexDto.UUID:
				case TaskIndexDto.ASSIGNEE_REPLY:
				case TaskIndexDto.CREATOR_COMMENT:
				case TaskIndexDto.PRIORITY:
				case TaskIndexDto.DUE_DATE:
				case TaskIndexDto.SUGGESTED_START:
				case TaskIndexDto.TASK_CONTEXT:
				case TaskIndexDto.TASK_STATUS:
				case TaskIndexDto.TASK_TYPE:
					expression = task.get(sortProperty.propertyName);
					break;
				case TaskIndexDto.ASSIGNEE_USER:
					expression = joins.getAssignee().get(User.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getAssignee().get(User.FIRST_NAME);
					break;
				case TaskIndexDto.CREATOR_USER:
					expression = joins.getCreator().get(User.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getCreator().get(User.FIRST_NAME);
					break;
				case TaskIndexDto.CAZE:
					expression = joins.getCasePerson().get(Person.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getCasePerson().get(Person.FIRST_NAME);
					break;
				case TaskIndexDto.CONTACT:
					expression = joins.getContactPerson().get(Person.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getContactPerson().get(Person.FIRST_NAME);
					break;
				case TaskIndexDto.EVENT:
					expression = joins.getEvent().get(Event.START_DATE);
					break;
				case TaskIndexDto.DISTRICT:
					expression = district;
					break;
				case TaskIndexDto.REGION:
					expression = region;
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
		}
		order.add(cb.desc(task.get(Task.DUE_DATE)));
		cq.orderBy(order);

		List<TaskIndexDto> tasks = QueryHelper.getResultList(em, cq, first, max);

		if (!tasks.isEmpty()) {
			List<String> assigneeUserUuids = tasks.stream().map(t -> t.getAssigneeUser().getUuid()).collect(Collectors.toList());
			Map<String, Long> pendingTaskCounts = getPendingTaskCountPerUser(assigneeUserUuids);

			for (TaskIndexDto singleTask : tasks) {
				// Workaround for Vaadin renderers not having access to their row reference; we therefore update the caption
				// directly instead of storing the task count in TaskIndexDto
				UserReferenceDto assigneeUser = singleTask.getAssigneeUser();
				Long taskCount = pendingTaskCounts.get(assigneeUser.getUuid());
				assigneeUser.setCaption(assigneeUser.getCaption() + " (" + (taskCount != null ? taskCount.toString() : "") + ")");
			}

			Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
			Pseudonymizer emptyValuePseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
			pseudonymizer
				.pseudonymizeDtoCollection(TaskIndexDto.class, tasks, t -> t.getTaskJurisdictionFlagsDto().getInJurisdiction(), (t, ignored) -> {
					final TaskJurisdictionFlagsDto taskJurisdictionFlagsDto = t.getTaskJurisdictionFlagsDto();
					if (t.getCaze() != null) {
						emptyValuePseudonymizer
							.pseudonymizeDto(CaseReferenceDto.class, t.getCaze(), taskJurisdictionFlagsDto.getCaseInJurisdiction(), null);
					}

					if (t.getContact() != null) {
						pseudonymizeContactReference(
							emptyValuePseudonymizer,
							t.getContact(),
							taskJurisdictionFlagsDto.getContactInJurisdiction(),
							taskJurisdictionFlagsDto.getContactCaseInJurisdiction());
					}

					if (t.getEvent() != null) {
						emptyValuePseudonymizer
							.pseudonymizeDto(EventReferenceDto.class, t.getEvent(), taskJurisdictionFlagsDto.getEventInJurisdiction(), null);
					}

					if (t.getTravelEntry() != null) {
						emptyValuePseudonymizer.pseudonymizeDto(
							TravelEntryReferenceDto.class,
							t.getTravelEntry(),
							taskJurisdictionFlagsDto.getTravelEntryInJurisdiction(),
							null);
					}
				}, true);
		}

		return tasks;
	}

	@Override
	public List<TaskExportDto> getExportList(TaskCriteria criteria, Collection<String> selectedRows, int first, int max) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TaskExportDto> cq = cb.createQuery(TaskExportDto.class);
		Root<Task> task = cq.from(Task.class);

		TaskQueryContext<Task> taskQueryContext = new TaskQueryContext<>(cb, cq, task);
		TaskJoins<Task> joins = (TaskJoins<Task>) taskQueryContext.getJoins();
		CaseQueryContext<Task> caseQueryContext = new CaseQueryContext<>(cb, cq, joins.getCaze());
		ContactQueryContext<Task> contactQueryContext = new ContactQueryContext<>(cb, cq, joins.getContact());

		LocationJoins<Person> casePersonAddressJoins = joins.getCasePersonJoins().getAddressJoins();
		LocationJoins<Person> contactPersonAddressJoins = joins.getContactPersonJoins().getAddressJoins();

		//@formatter:off
		cq.multiselect(task.get(Task.UUID), task.get(Task.TASK_CONTEXT),
				joins.getCaze().get(Case.UUID), joins.getContact().get(Contact.UUID), joins.getEvent().get(Event.UUID),
				task.get(Task.TASK_TYPE), task.get(Task.PRIORITY), task.get(Task.DUE_DATE), task.get(Task.SUGGESTED_START), task.get(Task.TASK_STATUS),
				joins.getCreator().get(User.UUID), joins.getCreator().get(User.FIRST_NAME), joins.getCreator().get(User.LAST_NAME),
				task.get(Task.CREATOR_COMMENT),
				joins.getAssignee().get(User.UUID), joins.getAssignee().get(User.FIRST_NAME), joins.getAssignee().get(User.LAST_NAME),
				task.get(Task.ASSIGNEE_REPLY),
				CriteriaBuilderHelper.coalesce(cb, joins.getCaseRegion().get(Region.NAME), joins.getContactRegion().get(Region.NAME), joins.getEventRegion().get(Region.NAME)),
				CriteriaBuilderHelper.coalesce(cb, joins.getCaseDistrict().get(District.NAME), joins.getContactDistrict().get(District.NAME), joins.getEventDistrict().get(District.NAME)),
				CriteriaBuilderHelper.coalesce(cb, joins.getCaseCommunity().get(Community.NAME), joins.getContactCommunity().get(Community.NAME), joins.getEventCommunity().get(Community.NAME)),
				getPersonFieldPath(cb, joins, Person.FIRST_NAME), getPersonFieldPath(cb, joins, Person.LAST_NAME), getPersonFieldPath(cb, joins, Person.SEX),
				getPersonFieldPath(cb, joins, Person.BIRTHDATE_DD), getPersonFieldPath(cb, joins, Person.BIRTHDATE_MM), getPersonFieldPath(cb, joins, Person.BIRTHDATE_YYYY),
				CriteriaBuilderHelper.coalesce(cb, casePersonAddressJoins.getRegion().get(Region.NAME), contactPersonAddressJoins.getRegion().get(Region.NAME)),
				CriteriaBuilderHelper.coalesce(cb, casePersonAddressJoins.getDistrict().get(District.NAME), contactPersonAddressJoins.getDistrict().get(District.NAME)),
				CriteriaBuilderHelper.coalesce(cb, casePersonAddressJoins.getCommunity().get(Community.NAME), contactPersonAddressJoins.getCommunity().get(Community.NAME)),
				CriteriaBuilderHelper.coalesce(cb, casePersonAddressJoins.getFacility().get(Facility.NAME), contactPersonAddressJoins.getFacility().get(Facility.NAME)),
				getPersonAddressFieldPath(cb, joins, Location.FACILITY_DETAILS),
				getPersonAddressFieldPath(cb, joins, Location.CITY), getPersonAddressFieldPath(cb, joins, Location.STREET), getPersonAddressFieldPath(cb, joins, Location.HOUSE_NUMBER),
				getPersonAddressFieldPath(cb, joins, Location.POSTAL_CODE),
				CriteriaBuilderHelper.coalesce(cb, caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_SUBQUERY), contactQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_SUBQUERY)),
				CriteriaBuilderHelper.coalesce(cb, caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_OWNER_SUBQUERY), contactQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_OWNER_SUBQUERY)),
				CriteriaBuilderHelper.coalesce(cb, caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_EMAIL_SUBQUERY), contactQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_EMAIL_SUBQUERY)),
				CriteriaBuilderHelper.coalesce(cb, caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_OTHER_CONTACT_DETAILS_SUBQUERY), contactQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_OTHER_CONTACT_DETAILS_SUBQUERY)),
				JurisdictionHelper.booleanSelector(cb, taskService.inJurisdictionOrOwned(taskQueryContext))
			);
		//@formatter:on
		Predicate filter = null;
		if (criteria == null || !criteria.hasContextCriteria()) {
			filter = taskService.createUserFilter(cb, cq, task);
		} else {
			filter = CriteriaBuilderHelper.and(cb, filter, taskService.createAssigneeFilter(cb, joins.getAssignee()));
		}

		if (criteria != null) {
			Predicate criteriaFilter = taskService.buildCriteriaFilter(criteria, cb, task, joins);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		// Distinct is necessary here to avoid duplicate results due to the user role join in taskService.createAssigneeFilter
		cq.distinct(true);

		cq.orderBy(cb.desc(task.get(Task.DUE_DATE)));

		List<TaskExportDto> tasks = QueryHelper.getResultList(em, cq, first, max);

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
		pseudonymizer.pseudonymizeDtoCollection(
			TaskExportDto.class,
			tasks,
			TaskExportDto::isInJurisdiction,
			(t, inJurisdiction) -> pseudonymizer.pseudonymizeDto(BirthDateDto.class, t.getPersonBirthDate(), inJurisdiction, null));

		return tasks;
	}

	private Expression<String> getPersonFieldPath(CriteriaBuilder cb, TaskJoins<?> joins, String fieldName) {
		return CriteriaBuilderHelper.coalesce(cb, joins.getCasePerson().get(fieldName), joins.getContactPerson().get(fieldName));
	}

	private Expression<String> getPersonAddressFieldPath(CriteriaBuilder cb, TaskJoins<?> joins, String fieldName) {
		return CriteriaBuilderHelper.coalesce(cb, joins.getCasePersonAddress().get(fieldName), joins.getContactPersonAddress().get(fieldName));
	}

	private void pseudonymizeContactReference(
		Pseudonymizer pseudonymizer,
		ContactReferenceDto contactReference,
		boolean isContactInJurisdiction,
		boolean isContactCaseInJurisdiction) {
		pseudonymizer.pseudonymizeDto(ContactReferenceDto.PersonName.class, contactReference.getContactName(), isContactInJurisdiction, null);

		if (contactReference.getCaseName() != null) {
			pseudonymizer.pseudonymizeDto(ContactReferenceDto.PersonName.class, contactReference.getCaseName(), isContactCaseInJurisdiction, null);
		}
	}

	@Override
	public List<TaskDto> getAllByCase(CaseReferenceDto caseRef) {

		if (caseRef == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return taskService.findBy(new TaskCriteria().caze(caseRef), false).stream().map(c -> toDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<TaskDto> getAllByContact(ContactReferenceDto contactRef) {

		if (contactRef == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return taskService.findBy(new TaskCriteria().contact(contactRef), false)
			.stream()
			.map(c -> toDto(c, pseudonymizer))
			.collect(Collectors.toList());
	}

	@Override
	public List<TaskDto> getAllByEvent(EventReferenceDto eventRef) {

		if (eventRef == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return taskService.findBy(new TaskCriteria().event(eventRef), false).stream().map(c -> toDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<TaskDto> getByUuids(List<String> uuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return taskService.getByUuids(uuids).stream().map(c -> toDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<TaskDto> getAllPendingByCase(CaseReferenceDto caseRef) {

		if (caseRef == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return taskService.findBy(new TaskCriteria().caze(caseRef).taskStatus(TaskStatus.PENDING), false)
			.stream()
			.map(c -> toDto(c, pseudonymizer))
			.collect(Collectors.toList());
	}

	@Override
	public long getPendingTaskCountByContact(ContactReferenceDto contactRef) {

		if (contactRef == null) {
			return 0;
		}

		return taskService.getCount(new TaskCriteria().contact(contactRef).taskStatus(TaskStatus.PENDING));
	}

	@Override
	public long getPendingTaskCountByEvent(EventReferenceDto eventRef) {

		if (eventRef == null) {
			return 0;
		}

		return taskService.getCount(new TaskCriteria().event(eventRef).taskStatus(TaskStatus.PENDING));
	}

	@Override
	public Map<String, Long> getPendingTaskCountPerUser(List<String> userUuids) {

		Map<String, Long> taskCountMap = new HashMap<>();

		IterableHelper.executeBatched(userUuids, ModelConstants.PARAMETER_LIMIT, batchedUserUuids -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
			Root<Task> from = cq.from(Task.class);
			Join<Task, User> userJoin = from.join(Task.ASSIGNEE_USER, JoinType.LEFT);

			cq.where(cb.equal(from.get(Task.TASK_STATUS), TaskStatus.PENDING), userJoin.get(User.UUID).in(batchedUserUuids));
			cq.multiselect(userJoin.get(User.UUID), cb.count(from));
			cq.groupBy(userJoin.get(User.UUID));

			List<Object[]> resultList = em.createQuery(cq).getResultList();
			resultList.forEach(r -> taskCountMap.put((String) r[0], (Long) r[1]));
		});

		return taskCountMap;
	}

	@Override
	public TaskDto getByUuid(String uuid) {
		return toDto(taskService.getByUuid(uuid), Pseudonymizer.getDefault(userService::hasRight));
	}

	@Override
	public void deleteTask(TaskDto taskDto) {

		if (!userService.hasRight(UserRight.TASK_DELETE)) {
			throw new UnsupportedOperationException("User " + userService.getCurrentUser().getUuid() + " is not allowed to delete tasks.");
		}

		Task task = taskService.getByUuid(taskDto.getUuid());
		taskService.delete(task);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void sendNewAndDueTaskMessages() {

		Calendar calendar = Calendar.getInstance();
		Date now = new Date();
		calendar.setTime(now);
		calendar.add(Calendar.MINUTE, CronService.TASK_UPDATE_INTERVAL * -1);
		Date before = calendar.getTime();

		List<Task> startingTasks = taskService.findBy(new TaskCriteria().taskStatus(TaskStatus.PENDING).startDateBetween(before, now), true);
		for (Task task : startingTasks) {
			TaskContext context = task.getTaskContext();
			AbstractDomainObject associatedEntity = context == TaskContext.CASE
				? task.getCaze()
				: context == TaskContext.CONTACT ? task.getContact() : context == TaskContext.EVENT ? task.getEvent() : null;
			if (task.getAssigneeUser() != null) {
				try {
					String content = context == TaskContext.GENERAL
						? String.format(I18nProperties.getString(MessagingService.CONTENT_TASK_START_GENERAL), task.getTaskType().toString())
						: String.format(
							I18nProperties.getString(MessagingService.CONTENT_TASK_START_SPECIFIC),
							task.getTaskType().toString(),
							buildAssociatedEntityLinkContent(context, associatedEntity));

					messagingService.sendMessage(
						userService.getByUuid(task.getAssigneeUser().getUuid()),
						MessageSubject.TASK_START,
						content,
						MessageType.EMAIL,
						MessageType.SMS);
				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"EmailDeliveryFailedException when trying to notify a user about a starting task. " + "Failed to send "
								+ e.getMessageType() + " to user with UUID %s.",
							task.getAssigneeUser().getUuid()));
				}
			}
		}

		List<Task> dueTasks = taskService.findBy(new TaskCriteria().taskStatus(TaskStatus.PENDING).dueDateBetween(before, now), true);
		for (Task task : dueTasks) {
			TaskContext context = task.getTaskContext();
			AbstractDomainObject associatedEntity = context == TaskContext.CASE
				? task.getCaze()
				: context == TaskContext.CONTACT ? task.getContact() : context == TaskContext.EVENT ? task.getEvent() : null;
			if (task.getAssigneeUser() != null) {
				try {
					String content = context == TaskContext.GENERAL
						? String.format(I18nProperties.getString(MessagingService.CONTENT_TASK_DUE_GENERAL), task.getTaskType().toString())
						: String.format(
							I18nProperties.getString(MessagingService.CONTENT_TASK_DUE_SPECIFIC),
							task.getTaskType().toString(),
							buildAssociatedEntityLinkContent(context, associatedEntity));

					messagingService.sendMessage(
						userService.getByUuid(task.getAssigneeUser().getUuid()),
						MessageSubject.TASK_DUE,
						content,
						MessageType.EMAIL,
						MessageType.SMS);
				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"EmailDeliveryFailedException when trying to notify a user about a due task. " + "Failed to send " + e.getMessageType()
								+ " to user with UUID %s.",
							task.getAssigneeUser().getUuid()));
				}
			}
		}
	}

	private void validate(TaskDto task) throws ValidationRuntimeException {

		if (task.getTaskContext() == TaskContext.CASE && task.getCaze() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.taskMissingCaseLink));
		}
		if (task.getTaskContext() == TaskContext.CONTACT && task.getContact() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.taskMissingContactLink));
		}
		if (task.getTaskContext() == TaskContext.EVENT && task.getEvent() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.taskMissingEventLink));
		}
	}

	@Override
	public void updateArchived(List<String> taskUuids, boolean archived) {
		IterableHelper.executeBatched(taskUuids, ARCHIVE_BATCH_SIZE, e -> taskService.updateArchived(e, archived));
	}

	@Override
	public List<DistrictReferenceDto> getDistrictsByTaskUuids(List<String> taskUuids, Long limit) {
		Set<String> districtUuids = new HashSet<>();
		IterableHelper.executeBatched(taskUuids, ModelConstants.PARAMETER_LIMIT, batchedTaskUuids -> {
			if (districtUuids.size() >= limit) {
				return;
			}

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);
			Root<Task> from = cq.from(Task.class);
			Join<Task, Case> caseJoin = from.join(Task.CAZE, JoinType.LEFT);
			Join<Case, District> caseDistrictJoin = caseJoin.join(Case.DISTRICT, JoinType.LEFT);
			Join<Task, Contact> contactJoin = from.join(Task.CONTACT, JoinType.LEFT);
			Join<Contact, District> contactDistrictJoin = contactJoin.join(Contact.DISTRICT, JoinType.LEFT);
			Join<Task, Event> eventJoin = from.join(Task.EVENT, JoinType.LEFT);
			Join<Event, Location> eventLocationJoin = eventJoin.join(Event.EVENT_LOCATION, JoinType.LEFT);
			Join<Location, District> eventDistrictJoin = eventLocationJoin.join(Location.DISTRICT, JoinType.LEFT);

			cq.where(
				cb.and(
					from.get(Task.UUID).in(taskUuids),
					cb.or(
						caseDistrictJoin.get(District.UUID).isNotNull(),
						contactDistrictJoin.get(District.UUID).isNotNull(),
						eventDistrictJoin.get(District.UUID).isNotNull())));
			cq.select(
				cb.coalesce(
					cb.coalesce(caseDistrictJoin.get(District.UUID), contactDistrictJoin.get(District.UUID)),
					eventDistrictJoin.get(District.UUID)))
				.distinct(true);

			List<String> batchedDistrictUuids = em.createQuery(cq).setMaxResults(limit.intValue()).getResultList();
			districtUuids.addAll(batchedDistrictUuids);
		});

		return districtUuids.stream().map(DistrictReferenceDto::new).limit(limit).collect(Collectors.toList());
	}

	@Override
	public List<String> getArchivedUuidsSince(Date since) {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return taskService.getArchivedUuidsSince(since);
	}

	private String buildAssociatedEntityLinkContent(TaskContext taskContext, AbstractDomainObject entity) {
		StringBuilder contentBuilder = new StringBuilder().append(taskContext);
		if (taskContext.getUrlPattern() == null || entity == null) {
			return contentBuilder.toString();
		}

		contentBuilder.append(" ").append(DataHelper.getShortUuid(entity.getUuid()));

		String url = getUiUrl(taskContext, entity.getUuid());
		if (url != null) {
			String associatedEntityLinkMessage = taskContext.getAssociatedEntityLinkMessage();
			contentBuilder.append("\n").append(String.format(I18nProperties.getString(associatedEntityLinkMessage), url));
		}

		return contentBuilder.toString();
	}

	/**
	 * Return the url of the related entity.
	 * The url is bound to the Sormas UI made with Vaadin.
	 * This function will need to be modified if the UI will have URL modifications
	 * or in case the UI app is replaced by another one.
	 */
	private String getUiUrl(TaskContext taskContext, String uuid) {
		if (taskContext.getUrlPattern() == null || uuid == null) {
			return null;
		}

		String uiUrl = configFacade.getUiUrl();
		if (uiUrl == null) {
			return null;
		}

		StringBuilder uiUrlBuilder = new StringBuilder(uiUrl);
		if (!uiUrl.endsWith("/")) {
			uiUrlBuilder.append("/");
		}
		return uiUrlBuilder.append("#!").append(taskContext.getUrlPattern()).append("/data/").append(uuid).toString();
	}

	@LocalBean
	@Stateless
	public static class TaskFacadeEjbLocal extends TaskFacadeEjb {

	}
}
