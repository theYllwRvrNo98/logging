请求参数和响应参数的的打印原理：
    requestBody和responseBody可能是json这种直接可读的，也可能是protobuf这种不可读的，所以打印的时机最好是打印业务对象
    requestBody 通过HttpMessageConverter 转换成业务对象后进行打印
    业务对象 通过HttpMessageConverter 转换成responseBody之前进行打印
    
    
    
    spring 关键代码
    请求参数打印
    AbstractMessageConverterMethodArgumentResolver.readWithMessageConverters() {
    ......
    for (HttpMessageConverter<?> converter : this.messageConverters) {
    				Class<HttpMessageConverter<?>> converterType = (Class<HttpMessageConverter<?>>) converter.getClass();
    				GenericHttpMessageConverter<?> genericConverter =
    						(converter instanceof GenericHttpMessageConverter ? (GenericHttpMessageConverter<?>) converter : null);
    				if (genericConverter != null ? genericConverter.canRead(targetType, contextClass, contentType) :
    						(targetClass != null && converter.canRead(targetClass, contentType))) {
    					if (logger.isDebugEnabled()) {
    						logger.debug("Read [" + targetType + "] as \"" + contentType + "\" with [" + converter + "]");
    					}
    					if (message.hasBody()) {
    						HttpInputMessage msgToUse =
    								getAdvice().beforeBodyRead(message, parameter, targetType, converterType);
    						body = (genericConverter != null ? genericConverter.read(targetType, contextClass, msgToUse) :
    								((HttpMessageConverter<T>) converter).read(targetClass, msgToUse));
    						//关键代码，可以getAdvice()里面加一个advice来打印请求参数
    						body = getAdvice().afterBodyRead(body, msgToUse, parameter, targetType, converterType);
    					}
    					else {
    						body = getAdvice().handleEmptyBody(null, message, parameter, targetType, converterType);
    					}
    					break;
    				}
    			}
    ......
    }
    
    
    响应参数打印
    AbstractMessageConverterMethodProcessor.writeWithMessageConverters() {
    ......
    for (HttpMessageConverter<?> converter : this.messageConverters) {
    				GenericHttpMessageConverter genericConverter =
    						(converter instanceof GenericHttpMessageConverter ? (GenericHttpMessageConverter<?>) converter : null);
    				if (genericConverter != null ?
    						((GenericHttpMessageConverter) converter).canWrite(declaredType, valueType, selectedMediaType) :
    						converter.canWrite(valueType, selectedMediaType)) {
    					//关键代码，可以getAdvice()里面加一个advice来打印请求参数
    					outputValue = (T) getAdvice().beforeBodyWrite(outputValue, returnType, selectedMediaType,
    							(Class<? extends HttpMessageConverter<?>>) converter.getClass(),
    							inputMessage, outputMessage);
    					if (outputValue != null) {
    						addContentDispositionHeader(inputMessage, outputMessage);
    						if (genericConverter != null) {
    							genericConverter.write(outputValue, declaredType, selectedMediaType, outputMessage);
    						}
    						else {
    							((HttpMessageConverter) converter).write(outputValue, selectedMediaType, outputMessage);
    						}
    						if (logger.isDebugEnabled()) {
    							logger.debug("Written [" + outputValue + "] as \"" + selectedMediaType +
    									"\" using [" + converter + "]");
    						}
    					}
    					return;
    				}
    			}
    ......
    }
    
    
    从上面的源码可以看出，只要在getAdvice()的时候加上打印的advice就可以了
    
    
    经过一层层向上找
    RequestMappingHandlerAdapter.getDefaultArgumentResolvers() {
    // Annotation-based argument resolution
    		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
    		resolvers.add(new RequestParamMapMethodArgumentResolver());
    		resolvers.add(new PathVariableMethodArgumentResolver());
    		resolvers.add(new PathVariableMapMethodArgumentResolver());
    		resolvers.add(new MatrixVariableMethodArgumentResolver());
    		resolvers.add(new MatrixVariableMapMethodArgumentResolver());
    		resolvers.add(new ServletModelAttributeMethodProcessor(false));
    		
    		//关键代码 看起来只要在this.requestResponseBodyAdvice加上打印的advice就可以了
    		resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
    		resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));
    		//
    		resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
    		resolvers.add(new RequestHeaderMapMethodArgumentResolver());
    		resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
    		resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));
    		resolvers.add(new SessionAttributeMethodArgumentResolver());
    		resolvers.add(new RequestAttributeMethodArgumentResolver());
    
    		// Type-based argument resolution
    		resolvers.add(new ServletRequestMethodArgumentResolver());
    		resolvers.add(new ServletResponseMethodArgumentResolver());
    		resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
    		resolvers.add(new RedirectAttributesMethodArgumentResolver());
    		resolvers.add(new ModelMethodProcessor());
    		resolvers.add(new MapMethodProcessor());
    		resolvers.add(new ErrorsMethodArgumentResolver());
    		resolvers.add(new SessionStatusMethodArgumentResolver());
    		resolvers.add(new UriComponentsBuilderMethodArgumentResolver());
    
    		// Custom arguments
    		if (getCustomArgumentResolvers() != null) {
    			resolvers.addAll(getCustomArgumentResolvers());
    		}
    
    		// Catch-all
    		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
    		resolvers.add(new ServletModelAttributeMethodProcessor(true));
    
    		return resolvers;
    }
    
    //getDefaultArgumentResolvers()的调用时机
    public class RequestMappingHandlerAdapter {
        @Override
            public void afterPropertiesSet() {
                //关键代码,看源码注释
                // Do this first, it may add ResponseBody advice beans
                //所有可以在initControllerAdviceCache()方法里面加东西
                initControllerAdviceCache();
        
                if (this.argumentResolvers == null) {
                //关键代码
                    List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
                    this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
                }
                if (this.initBinderArgumentResolvers == null) {
                    List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers();
                    this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
                }
                if (this.returnValueHandlers == null) {
                    List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
                    this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
                }
            }
    	}
   
   //无法在构造this.requestResponseBodyAdvice对象的时候去添加
   public class WebMvcConfigurationSupport {
   
        @Bean
        	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        		RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
        		adapter.setContentNegotiationManager(mvcContentNegotiationManager());
        		adapter.setMessageConverters(getMessageConverters());
        		adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
        		adapter.setCustomArgumentResolvers(getArgumentResolvers());
        		adapter.setCustomReturnValueHandlers(getReturnValueHandlers());
        
        		if (jackson2Present) {
        			adapter.setRequestBodyAdvice(Collections.singletonList(new JsonViewRequestBodyAdvice()));
        			adapter.setResponseBodyAdvice(Collections.singletonList(new JsonViewResponseBodyAdvice()));
        		}
        
        		AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
        		configureAsyncSupport(configurer);
        		if (configurer.getTaskExecutor() != null) {
        			adapter.setTaskExecutor(configurer.getTaskExecutor());
        		}
        		if (configurer.getTimeout() != null) {
        			adapter.setAsyncRequestTimeout(configurer.getTimeout());
        		}
        		adapter.setCallableInterceptors(configurer.getCallableInterceptors());
        		adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());
        
        		return adapter;
        	}
   
   }
   
 
   public class RequestMappingHandlerAdapter {
   
       //这个方法里面提供了一些入口
       private void initControllerAdviceCache() {
            if (getApplicationContext() == null) {
                return;
            }
            if (logger.isInfoEnabled()) {
                logger.info("Looking for @ControllerAdvice: " + getApplicationContext());
            }
            
            //关键代码 在容器Spring中获取了所有的ControllerAdviceBean对象
            List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
            AnnotationAwareOrderComparator.sort(adviceBeans);
       
            List<Object> requestResponseBodyAdviceBeans = new ArrayList<>();
       
            for (ControllerAdviceBean adviceBean : adviceBeans) {
                Class<?> beanType = adviceBean.getBeanType();
                if (beanType == null) {
                    throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
                }
                Set<Method> attrMethods = MethodIntrospector.selectMethods(beanType, MODEL_ATTRIBUTE_METHODS);
                if (!attrMethods.isEmpty()) {
                    this.modelAttributeAdviceCache.put(adviceBean, attrMethods);
                    if (logger.isInfoEnabled()) {
                        logger.info("Detected @ModelAttribute methods in " + adviceBean);
                    }
                }
                Set<Method> binderMethods = MethodIntrospector.selectMethods(beanType, INIT_BINDER_METHODS);
                if (!binderMethods.isEmpty()) {
                    this.initBinderAdviceCache.put(adviceBean, binderMethods);
                    if (logger.isInfoEnabled()) {
                        logger.info("Detected @InitBinder methods in " + adviceBean);
                    }
                }
                //关键代码 添加RequestBodyAdvice
                if (RequestBodyAdvice.class.isAssignableFrom(beanType)) {
                    requestResponseBodyAdviceBeans.add(adviceBean);
                    if (logger.isInfoEnabled()) {
                        logger.info("Detected RequestBodyAdvice bean in " + adviceBean);
                    }
                }
                //关键代码 ResponseBodyAdvice
                if (ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
                    requestResponseBodyAdviceBeans.add(adviceBean);
                    if (logger.isInfoEnabled()) {
                        logger.info("Detected ResponseBodyAdvice bean in " + adviceBean);
                    }
                }
            }
            //关键代码
            if (!requestResponseBodyAdviceBeans.isEmpty()) {
                this.requestResponseBodyAdvice.addAll(0, requestResponseBodyAdviceBeans);
            }
        }
   
   }
   
   综上