package de.symeda.sormas.backend.person;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.AbstractAdoService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.event.EventParticipantService;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.user.User;

@Stateless
@LocalBean
public class PersonService extends AbstractAdoService<Person> {

	@EJB
	CaseService caseService;
	@EJB
	ContactService contactService;
	@EJB
	EventParticipantService eventParticipantService;

	public PersonService() {
		super(Person.class);
	}

	public Person createPerson() {
		Person person = new Person();
		return person;
	}

	
	public List<Person> getAllAfter(Date date, User user) {

		// TODO get user from session?

		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		// persons by LGA
		CriteriaQuery<Person> lgaQuery = cb.createQuery(Person.class);
		Root<Person> lgaRoot = lgaQuery.from(Person.class);
		Join<Person, Location> address = lgaRoot.join(Person.ADDRESS);
		Predicate lgaFilter = cb.equal(address.get(Location.DISTRICT), user.getDistrict());
		// date range
		if (date != null) {
			Predicate dateFilter = cb.greaterThan(lgaRoot.get(AbstractDomainObject.CHANGE_DATE), date);
			if (lgaFilter != null) {
				lgaFilter = cb.and(lgaFilter, dateFilter);
			} else {
				lgaFilter = dateFilter;
			}
		}
		lgaQuery.where(lgaFilter);
		lgaQuery.distinct(true);
		List<Person> lgaResultList = em.createQuery(lgaQuery).getResultList();
		
		// persons by case
		CriteriaQuery<Person> casePersonsQuery = cb.createQuery(Person.class);
		Root<Case> casePersonsRoot = casePersonsQuery.from(Case.class);
		Path<Person> casePersonsSelect = casePersonsRoot.get(Case.PERSON);
		casePersonsQuery.select(casePersonsSelect);
		Predicate casePersonsFilter = caseService.createUserFilter(cb, casePersonsRoot, user);
		// date range
		if (date != null) {
			Predicate dateFilter = cb.greaterThan(casePersonsSelect.get(AbstractDomainObject.CHANGE_DATE), date);
			if (casePersonsFilter != null) {
				casePersonsFilter = cb.and(casePersonsFilter, dateFilter);
			} else {
				casePersonsFilter = dateFilter;
			}
		}
		casePersonsQuery.where(casePersonsFilter);
		casePersonsQuery.distinct(true);
		List<Person> casePersonsResultList = em.createQuery(casePersonsQuery).getResultList();

		// persons by contact
		CriteriaQuery<Person> contactPersonsQuery = cb.createQuery(Person.class);
		Root<Contact> contactPersonsRoot = contactPersonsQuery.from(Contact.class);
		Path<Person> contactPersonsSelect = contactPersonsRoot.get(Contact.PERSON);
		contactPersonsQuery.select(contactPersonsSelect);
		Predicate contactPersonsFilter = contactService.createUserFilter(cb, contactPersonsRoot, user);
		// date range
		if (date != null) {
			Predicate dateFilter = cb.greaterThan(contactPersonsSelect.get(AbstractDomainObject.CHANGE_DATE), date);
			if (contactPersonsFilter != null) {
				contactPersonsFilter = cb.and(contactPersonsFilter, dateFilter);
			} else {
				contactPersonsFilter = dateFilter;
			}
		}
		contactPersonsQuery.where(contactPersonsFilter);
		contactPersonsQuery.distinct(true);
		List<Person> contactPersonsResultList = em.createQuery(contactPersonsQuery).getResultList();

		// persons by event participant
		CriteriaQuery<Person> eventPersonsQuery = cb.createQuery(Person.class);
		Root<EventParticipant> eventPersonsRoot = eventPersonsQuery.from(EventParticipant.class);
		Path<Person> eventPersonsSelect = eventPersonsRoot.get(EventParticipant.PERSON);
		eventPersonsQuery.select(eventPersonsSelect);
		Predicate eventPersonsFilter = eventParticipantService.createUserFilter(cb, eventPersonsRoot, user);
		// date range
		if (date != null) {
			Predicate dateFilter = cb.greaterThan(eventPersonsSelect.get(AbstractDomainObject.CHANGE_DATE), date);
			if (eventPersonsFilter != null) {
				eventPersonsFilter = cb.and(eventPersonsFilter, dateFilter);
			} else {
				eventPersonsFilter = dateFilter;
			}
		}
		eventPersonsQuery.where(eventPersonsFilter);
		eventPersonsQuery.distinct(true);
		List<Person> eventPersonsResultList = em.createQuery(eventPersonsQuery).getResultList();
		
		return Stream.of(lgaResultList, casePersonsResultList, contactPersonsResultList, eventPersonsResultList)
				.flatMap(List<Person>::stream)
				.distinct()
				.sorted(Comparator.comparing(Person::getId))
				.collect(Collectors.toList());
	}
	
	public List<Person> getDeathsBetween(Date fromDate, Date toDate, Disease disease, User user) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<Person> casePersonsQuery = cb.createQuery(Person.class);
		Root<Case> casePersonsRoot = casePersonsQuery.from(Case.class);
		Path<Person> casePersonsSelect = casePersonsRoot.get(Case.PERSON);
		casePersonsQuery.select(casePersonsSelect);
		Predicate casePersonsFilter = caseService.createUserFilter(cb, casePersonsRoot, user);
		
		// only probable and confirmed cases are of interest
		Predicate classificationFilter = cb.equal(casePersonsRoot.get(Case.CASE_CLASSIFICATION), CaseClassification.CONFIRMED);
		classificationFilter = cb.or(classificationFilter, cb.equal(casePersonsRoot.get(Case.CASE_CLASSIFICATION), CaseClassification.PROBABLE));
		
		if (casePersonsFilter != null) {
			casePersonsFilter = cb.and(casePersonsFilter, classificationFilter);
		} else {
			casePersonsFilter = classificationFilter;
		}
		
		// death date range
		Predicate dateFilter = cb.isNotNull(casePersonsSelect.get(Person.DEATH_DATE));
		dateFilter = cb.and(dateFilter, cb.greaterThanOrEqualTo(casePersonsSelect.get(Person.DEATH_DATE), fromDate));
		dateFilter = cb.and(dateFilter, cb.lessThanOrEqualTo(casePersonsSelect.get(Person.DEATH_DATE), toDate));
		
		if (casePersonsFilter != null) {
			casePersonsFilter = cb.and(casePersonsFilter, dateFilter);
		} else {
			casePersonsFilter = dateFilter;
		}
		
		if (casePersonsFilter != null && disease != null) {
			casePersonsFilter = cb.and(casePersonsFilter, cb.equal(casePersonsRoot.get(Case.DISEASE), disease));
		}
		
		casePersonsQuery.where(casePersonsFilter);
		casePersonsQuery.distinct(true);
		List<Person> casePersonsResultList = em.createQuery(casePersonsQuery).getResultList();
		return casePersonsResultList;
	}
	
}
