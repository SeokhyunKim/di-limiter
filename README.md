# dicounter
Distributed counting library for arbitrary events. Consider a set of hosts that are observing some sort of events.
What dicounter wants to do is let the hosts know when the total number of the events being observed by multiple hosts
reached to a predefined threshold, let those hosts know it.
So, dicounter can be used for many use cases. One immediate application would be distributed rate limiter.