/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.subscription.repository.dao;

import gov.hhs.fha.nhinc.subscription.repository.data.NotificationStorageItem;
import gov.hhs.fha.nhinc.subscription.repository.persistence.HibernateUtil;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 * Data access object class for notification storage items
 *
 * @author richard.ettema
 */
public class NotificationStorageItemDao {

    private static final Logger LOG = Logger.getLogger(NotificationStorageItemDao.class);

    /**
     * Store a notification storage item.
     *
     * @param notificationItem
     */
    public void save(NotificationStorageItem notificationItem) {
        LOG.debug("Performing notification item save");
        Session sess = null;
        Transaction trans = null;
        try {
            SessionFactory fact = HibernateUtil.getSessionFactory();
            if (fact != null) {
                sess = fact.openSession();
                if (sess != null) {
                    trans = sess.beginTransaction();
                    sess.saveOrUpdate(notificationItem);
                } else {
                    LOG.error("Failed to obtain a session from the sessionFactory");
                }
            } else {
                LOG.error("Session factory was null");
            }
        } finally {
            if (trans != null) {
                try {
                    trans.commit();
                } catch (Throwable t) {
                    LOG.error("Failed to commit transaction: " + t.getMessage(), t);
                }
            }
            if (sess != null) {
                try {
                    sess.close();
                } catch (Throwable t) {
                    LOG.error("Failed to close session: " + t.getMessage(), t);
                }
            }
        }

        LOG.debug("Completed notification item save");
    }

    /**
     * Retrieve notification storage items by basic search criteria
     *
     * @param startNotificationDate
     * @param stopNotificationDate
     * @param notificationStatus
     * @return Retrieved notifications
     */
    @SuppressWarnings({ "unchecked" })
    public List<NotificationStorageItem> findByCriteria(Date startNotificationDate, Date stopNotificationDate, String notificationStatus) {
        LOG.debug("Performing subscription retrieve using criteria: startNotificationDate = '" + startNotificationDate
                + "'; stopNotificationDate = '" + stopNotificationDate + "'; notificationStatus = '" + notificationStatus + "'");

        List<NotificationStorageItem> notifications = null;
        Session sess = null;
        try {
            SessionFactory fact = HibernateUtil.getSessionFactory();
            if (fact != null) {
                sess = fact.openSession();
                if (sess != null) {
                    Criteria criteria = sess.createCriteria(NotificationStorageItem.class);
                    if (startNotificationDate != null) {
                        criteria.add(Restrictions.ge("notificationTime", startNotificationDate));
                    }
                    if (stopNotificationDate != null) {
                        criteria.add(Restrictions.le("notificationTime", stopNotificationDate));
                    }
                    if (notificationStatus != null) {
                        criteria.add(Restrictions.eq("notificationStatus", notificationStatus));
                    }
                    notifications = criteria.list();
                } else {
                    LOG.error("Failed to obtain a session from the sessionFactory");
                }
            } else {
                LOG.error("Session factory was null");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Completed notification retrieve by criteria. Results found: "
                        + ((notifications == null) ? "0" : Integer.toString(notifications.size())));
            }
        } finally {
            if (sess != null) {
                try {
                    sess.close();
                } catch (Throwable t) {
                    LOG.error("Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        return notifications;
    }

    /**
     * Retrieve a notification storage item by identifier
     *
     * @param notificationId Notification database identifier
     * @return Retrieved notification
     */
    public NotificationStorageItem findById(Long notificationId) {
        LOG.debug("Performing notification retrieve using id: " + notificationId);
        NotificationStorageItem notification = null;
        Session sess = null;
        try {
            SessionFactory fact = HibernateUtil.getSessionFactory();
            if (fact != null) {
                sess = fact.openSession();
                if (sess != null) {
                    notification = (NotificationStorageItem) sess.get(NotificationStorageItem.class, notificationId);
                } else {
                    LOG.error("Failed to obtain a session from the sessionFactory");
                }
            } else {
                LOG.error("Session factory was null");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Completed notification retrieve by id. Result was " + ((notification == null) ? "not " : "")
                        + "found");
            }
        } finally {
            if (sess != null) {
                try {
                    sess.close();
                } catch (Throwable t) {
                    LOG.error("Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        return notification;
    }

    /**
     * Retrieve notification storage items by subscription identifier
     *
     * @param subscriptionId Subscription identifier
     * @return Retrieved notifications
     */
    @SuppressWarnings({ "unchecked" })
    public List<NotificationStorageItem> findBySubscriptionId(String subscriptionId) {
        LOG.debug("Performing notification retrieve using subscription id: " + subscriptionId);
        List<NotificationStorageItem> notifications = null;
        Session sess = null;
        try {
            SessionFactory fact = HibernateUtil.getSessionFactory();
            if (fact != null) {
                sess = fact.openSession();
                if (sess != null) {
                    Criteria criteria = sess.createCriteria(NotificationStorageItem.class);
                    criteria.add(Restrictions.eq("subscriptionId", subscriptionId));
                    notifications = criteria.list();
                } else {
                    LOG.error("Failed to obtain a session from the sessionFactory");
                }
            } else {
                LOG.error("Session factory was null");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Completed notification retrieve by subscription id. Results found: "
                        + ((notifications == null) ? "0" : Integer.toString(notifications.size())));
            }
        } finally {
            if (sess != null) {
                try {
                    sess.close();
                } catch (Throwable t) {
                    LOG.error("Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        return notifications;
    }

    /**
     * Delete a notification storage item
     *
     * @param subscriptionItem notification storage item to delete
     */
    public void delete(NotificationStorageItem notificationItem) {
        LOG.debug("Performing notification storage item delete");

        Session sess = null;
        Transaction trans = null;
        try {
            SessionFactory fact = HibernateUtil.getSessionFactory();
            if (fact != null) {
                sess = fact.openSession();
                if (sess != null) {
                    trans = sess.beginTransaction();
                    sess.delete(notificationItem);
                } else {
                    LOG.error("Failed to obtain a session from the sessionFactory");
                }
            } else {
                LOG.error("Session factory was null");
            }
        } finally {
            if (trans != null) {
                try {
                    trans.commit();
                } catch (Throwable t) {
                    LOG.error("Failed to commit transaction: " + t.getMessage(), t);
                }
            }
            if (sess != null) {
                try {
                    sess.close();
                } catch (Throwable t) {
                    LOG.error("Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        LOG.debug("Completed notification storage item delete");
    }

}
