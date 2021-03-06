struct mutex;
struct kref;
typedef struct {
	int counter;
} atomic_t;

int __VERIFIER_nondet_int(void);

extern void mutex_lock(struct mutex *lock);
extern void mutex_lock_nested(struct mutex *lock, unsigned int subclass);
extern int mutex_lock_interruptible(struct mutex *lock);
extern int mutex_lock_killable(struct mutex *lock);
extern int mutex_lock_interruptible_nested(struct mutex *lock, unsigned int subclass);
extern int mutex_lock_killable_nested(struct mutex *lock, unsigned int subclass);
static inline int mutex_is_locked(struct mutex *lock);
extern int mutex_trylock(struct mutex *lock);
extern void mutex_unlock(struct mutex *lock);
extern int atomic_dec_and_mutex_lock(atomic_t *cnt, struct mutex *lock);
static inline int kref_put_mutex(struct kref *kref,
				 void (*release)(struct kref *kref),
				 struct mutex *lock);

static void specific_func(struct kref *kref);

void ldv_check_final_state(void);

void main(void)
{
	struct mutex *mutex_1, *mutex_2, *mutex_3, *mutex_4, *mutex_5;
	struct kref *kref;
	atomic_t *counter;
	
	mutex_lock(&mutex_1);
	mutex_lock_nested(&mutex_2, __VERIFIER_nondet_int());
	if (kref_put_mutex(kref, specific_func, &mutex_3))
	    mutex_unlock(&mutex_3);
	mutex_unlock(&mutex_1);
	mutex_unlock(&mutex_2);
	
	if (!mutex_lock_interruptible(&mutex_1))
		mutex_unlock(&mutex_1);
	if (!mutex_lock_killable(&mutex_1))
		mutex_unlock(&mutex_1);
	if (!mutex_lock_interruptible_nested(&mutex_1, __VERIFIER_nondet_int()))
		mutex_unlock(&mutex_1);
	if (!mutex_lock_killable_nested(&mutex_1, __VERIFIER_nondet_int()))
		mutex_unlock(&mutex_1);
	if (mutex_trylock(&mutex_2))
		mutex_unlock(&mutex_2);
	if (atomic_dec_and_mutex_lock(counter, &mutex_2))
		mutex_unlock(&mutex_2);
	mutex_lock(&mutex_3);
	if (mutex_is_locked(&mutex_3))
		mutex_unlock(&mutex_3);
	
	if (!mutex_lock_interruptible(&mutex_1)) {
		if (!mutex_lock_killable(&mutex_2)) {
			if (atomic_dec_and_mutex_lock(counter, &mutex_3)) {
				mutex_unlock(&mutex_3);
				if (!mutex_lock_killable_nested(&mutex_4, __VERIFIER_nondet_int())) {
					mutex_unlock(&mutex_4);
					if (!mutex_lock_killable_nested(&mutex_5, __VERIFIER_nondet_int())) {
						mutex_unlock(&mutex_5);
					} else if (!mutex_lock_killable(&mutex_5)) {
						mutex_unlock(&mutex_5);
					} else {
						mutex_lock(&mutex_5);
						mutex_unlock(&mutex_5);
					}
				}
			} else if (mutex_trylock(&mutex_3)) {
				mutex_unlock(&mutex_3);
			}
			mutex_unlock(&mutex_2);
		}
		mutex_unlock(&mutex_1);
	}

	ldv_check_final_state();
}

